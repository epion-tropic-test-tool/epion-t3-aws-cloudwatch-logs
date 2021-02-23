/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.runner;

import com.epion_t3.aws.core.configuration.AwsCredentialsProviderConfiguration;
import com.epion_t3.aws.core.holder.AwsCredentialsProviderHolder;
import com.epion_t3.aws.cwl.command.model.AwsCwlGetLastLogStreamEvents;
import com.epion_t3.aws.cwl.command.model.LogEventInfo;
import com.epion_t3.aws.cwl.command.model.LogStreamInfo;
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
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.OrderBy;

import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AwsCwlGetLogStreamEventsRunner extends AbstractCommandRunner<AwsCwlGetLastLogStreamEvents> {

    /**
     * オブジェクトマッパー.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public CommandResult execute(AwsCwlGetLastLogStreamEvents command, Logger logger) throws Exception {

        var awsCredentialsProviderConfiguration = (AwsCredentialsProviderConfiguration) referConfiguration(
                command.getCredentialsConfigRef());

        var credentialsProvider = AwsCredentialsProviderHolder.getInstance()
                .getCredentialsProvider(awsCredentialsProviderConfiguration);

        var cloudWatchLogs = CloudWatchLogsClient.builder().credentialsProvider(credentialsProvider).build();

        var getLogStreamRequestBuilder = DescribeLogStreamsRequest.builder().logGroupName(command.getLogGroupName());

        if (StringUtils.isNotEmpty(command.getLogStreamNamePrefix())) {
            getLogStreamRequestBuilder.logStreamNamePrefix(command.getLogStreamNamePrefix());
        }

        // 1固定
        getLogStreamRequestBuilder.limit(1);

        if (StringUtils.equalsAny(command.getOrderBy(), OrderBy.LAST_EVENT_TIME.toString(),
                OrderBy.LAST_EVENT_TIME.toString())) {
            getLogStreamRequestBuilder.orderBy(OrderBy.fromValue(command.getOrderBy()));
        }

        getLogStreamRequestBuilder.descending(command.isDescending());

        var logStreams = (List<LogStreamInfo>) null;

        var logStreamName = (Optional<String>) null;

        try {
            var getLogStreamReponse = cloudWatchLogs.describeLogStreamsPaginator(getLogStreamRequestBuilder.build());

            if (getLogStreamReponse.logStreams().stream().count() != 1) {
                throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9003, command.getLogGroupName());
            }

            logStreamName = getLogStreamReponse.logStreams().stream().map(LogStream::logStreamName).findFirst();

        } catch (Exception e) {
            throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9003, command.getLogGroupName());
        }

        var getLogEventsRequestBuilder = GetLogEventsRequest.builder()
                .logGroupName(command.getLogGroupName())
                .logStreamName(logStreamName.get());

        if (command.getEventsLimit() != null) {
            getLogEventsRequestBuilder.limit(command.getEventsLimit());
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
                    return new LogEventInfo(x.ingestionTime(), x.timestamp(), x.message(),
                            objectMapper.readValue(x.message(), (new LinkedHashMap<String, Object>(0)).getClass()));
                } catch (JsonProcessingException e) {
                    throw new SystemException();
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9002, command.getLogGroupName(), logStreamName);
        }

        var evidencePath = getEvidencePath("logStreams.json");

        // ファイルエビデンス書き出し
        try (var fos = new FileOutputStream(evidencePath.toFile())) {
            objectMapper.writeValue(fos, logStreams);
        }

        // オブジェクトエビデンス登録
        registrationObjectEvidence(logStreams);

        // ファイルエビデンス登録
        registrationFileEvidence(evidencePath);

        return CommandResult.getSuccess();
    }

}
