/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.runner;

import com.epion_t3.aws.core.configuration.AwsCredentialsProviderConfiguration;
import com.epion_t3.aws.core.configuration.AwsSdkHttpClientConfiguration;
import com.epion_t3.aws.core.holder.AwsCredentialsProviderHolder;
import com.epion_t3.aws.core.holder.AwsSdkHttpClientHolder;
import com.epion_t3.aws.cwl.bean.LogEventInfo;
import com.epion_t3.aws.cwl.bean.LogEventsInfo;
import com.epion_t3.aws.cwl.bean.LogStreamInfo;
import com.epion_t3.aws.cwl.command.model.AwsCwlGetLogStreamEvents;
import com.epion_t3.aws.cwl.messages.AwsCwlMessages;
import com.epion_t3.core.command.bean.CommandResult;
import com.epion_t3.core.command.runner.impl.AbstractCommandRunner;
import com.epion_t3.core.exception.SystemException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.OrderBy;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AwsCwlGetLogStreamEventsRunner extends AbstractCommandRunner<AwsCwlGetLogStreamEvents> {

    /**
     * オブジェクトマッパー.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public CommandResult execute(AwsCwlGetLogStreamEvents command, Logger logger) throws Exception {

        var awsCredentialsProviderConfiguration = (AwsCredentialsProviderConfiguration) referConfiguration(
                command.getCredentialsConfigRef());

        var credentialsProvider = AwsCredentialsProviderHolder.getInstance()
                .getCredentialsProvider(awsCredentialsProviderConfiguration);

        var cloudWatchLogs = (CloudWatchLogsClient) null;
        if (StringUtils.isEmpty(command.getSdkHttpClientConfigRef())) {
            cloudWatchLogs = CloudWatchLogsClient.builder().credentialsProvider(credentialsProvider).build();
        } else {
            var sdkHttpClientConfiguration = (AwsSdkHttpClientConfiguration) referConfiguration(
                    command.getSdkHttpClientConfigRef());
            var sdkHttpClient = AwsSdkHttpClientHolder.getInstance().getSdkHttpClient(sdkHttpClientConfiguration);
            cloudWatchLogs = CloudWatchLogsClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .httpClient(sdkHttpClient)
                    .build();
        }

        var getLogStreamRequestBuilder = DescribeLogStreamsRequest.builder().logGroupName(command.getLogGroupName());

        if (StringUtils.isNotEmpty(command.getLogStreamNamePrefix())) {
            getLogStreamRequestBuilder.logStreamNamePrefix(command.getLogStreamNamePrefix());
        }

        if (StringUtils.equalsAny(command.getOrderBy(), OrderBy.LAST_EVENT_TIME.toString(),
                OrderBy.LAST_EVENT_TIME.toString())) {
            getLogStreamRequestBuilder.orderBy(OrderBy.fromValue(command.getOrderBy()));
        }

        getLogStreamRequestBuilder.descending(command.isDescending());

        var logStreams = new ArrayList<LogStreamInfo>();

        try {
            var describeLogStreamResponse = (DescribeLogStreamsResponse) null;
            while (logStreams.size() < command.getStreamLimit()) {
                if (describeLogStreamResponse != null) {
                    getLogStreamRequestBuilder.nextToken(describeLogStreamResponse.nextToken());
                }
                describeLogStreamResponse = cloudWatchLogs.describeLogStreams(getLogStreamRequestBuilder.build());
                for (LogStream logStream : describeLogStreamResponse.logStreams()) {
                    logStreams.add(new LogStreamInfo(logStream.logStreamName(), logStream.creationTime(),
                            logStream.firstEventTimestamp(), logStream.lastEventTimestamp(),
                            logStream.lastIngestionTime(), logStream.uploadSequenceToken()));
                    if (logStreams.size() >= command.getStreamLimit()) {
                        // LogStreamの取得数が上限以上になった場合は、取得を終了
                        break;
                    }
                }
            }
            if (logStreams.isEmpty()) {
                throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9003, command.getLogGroupName());
            }
        } catch (Exception e) {
            throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9003, command.getLogGroupName());
        }

        var allLogEvent = new ArrayList<LogEventsInfo>();

        for (int i = 0; i < logStreams.size(); i++) {

            var logStreamInfo = logStreams.get(i);

            var logEventsInfo = new LogEventsInfo();
            allLogEvent.add(logEventsInfo);
            logEventsInfo.setLogGroupName(command.getLogGroupName());
            logEventsInfo.setLogStreamName(logStreamInfo.getLogStreamName());
            logEventsInfo.setCreationTime(logStreamInfo.getCreationTime());
            logEventsInfo.setFirstEventTimestamp(logStreamInfo.getFirstEventTimestamp());
            logEventsInfo.setLastEventTimestamp(logStreamInfo.getLastEventTimestamp());
            logEventsInfo.setLastIngestionTime(logStreamInfo.getLastIngestionTime());
            logEventsInfo.setUploadSequenceToken(logStreamInfo.getUploadSequenceToken());

            var getLogEventsRequestBuilder = GetLogEventsRequest.builder()
                    .logGroupName(command.getLogGroupName())
                    .logStreamName(logStreamInfo.getLogStreamName());

            if (command.getEventLimit() != null) {
                getLogEventsRequestBuilder.limit(command.getEventLimit());
            }

            if (command.getStartTime() != null) {
                getLogEventsRequestBuilder.startTime(command.getStartTime());
            }

            if (command.getEndTime() != null) {
                getLogEventsRequestBuilder.endTime(command.getEndTime());
            }

            if (command.getStartFromHead() != null) {
                getLogEventsRequestBuilder.startFromHead(command.getStartFromHead());
            }

            var logEvents = (List<LogEventInfo>) null;
            try {
                var getLogEventsResponse = cloudWatchLogs.getLogEventsPaginator(getLogEventsRequestBuilder.build());
                logEvents = getLogEventsResponse.events().stream().map(x -> {
                    try {
                        if (command.isJson()) {
                            return new LogEventInfo(x.ingestionTime(), x.timestamp(), x.message(), objectMapper
                                    .readValue(x.message(), (new LinkedHashMap<String, Object>(0)).getClass()));
                        } else {
                            return new LogEventInfo(x.ingestionTime(), x.timestamp(), x.message(), null);
                        }
                    } catch (JsonProcessingException e) {
                        throw new SystemException(e);
                    }
                }).collect(Collectors.toList());
            } catch (Exception e) {
                throw new SystemException(e, AwsCwlMessages.AWS_CWL_ERR_9002, command.getLogGroupName(),
                        logStreamInfo.getLogStreamName());
            }

            logEventsInfo.setEvents(logEvents);

            var evidencePath = getEvidencePath(String.format("logEvents-%03d.json", i + 1));

            // ファイルエビデンス書き出し
            try (var fos = new FileOutputStream(evidencePath.toFile())) {
                objectMapper.writeValue(fos, logEventsInfo);
            }

            // ファイルエビデンス登録
            registrationFileEvidence(evidencePath);
        }

        // オブジェクトエビデンス登録
        registrationObjectEvidence(allLogEvent);

        return CommandResult.getSuccess();
    }

}
