/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.runner;

import com.epion_t3.aws.core.configuration.AwsCredentialsProviderConfiguration;
import com.epion_t3.aws.core.holder.AwsCredentialsProviderHolder;
import com.epion_t3.aws.cwl.command.model.AwsCwlGetLogStream;
import com.epion_t3.aws.cwl.command.model.LogStreamInfo;
import com.epion_t3.aws.cwl.messages.AwsCwlMessages;
import com.epion_t3.core.command.bean.CommandResult;
import com.epion_t3.core.command.runner.impl.AbstractCommandRunner;
import com.epion_t3.core.exception.SystemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.OrderBy;

import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class AwsCwlGetLogStreamRunner extends AbstractCommandRunner<AwsCwlGetLogStream> {

    /**
     * オブジェクトマッパー.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public CommandResult execute(AwsCwlGetLogStream command, Logger logger) throws Exception {

        var awsCredentialsProviderConfiguration = (AwsCredentialsProviderConfiguration) referConfiguration(
                command.getCredentialsConfigRef());

        var credentialsProvider = AwsCredentialsProviderHolder.getInstance()
                .getCredentialsProvider(awsCredentialsProviderConfiguration);

        var cloudWatchLogs = CloudWatchLogsClient.builder().credentialsProvider(credentialsProvider).build();

        var requestBuilder = DescribeLogStreamsRequest.builder().logGroupName(command.getLogGroupName());

        if (StringUtils.isNotEmpty(command.getLogStreamNamePrefix())) {
            requestBuilder.logStreamNamePrefix(command.getLogStreamNamePrefix());
        }

        if (command.getLimit() != null) {
            requestBuilder.limit(command.getLimit());
        }

        if (StringUtils.equalsAny(command.getOrderBy(), OrderBy.LAST_EVENT_TIME.toString(),
                OrderBy.LAST_EVENT_TIME.toString())) {
            requestBuilder.orderBy(OrderBy.fromValue(command.getOrderBy()));
        }

        requestBuilder.descending(command.isDescending());

        var logStreams = (List<LogStreamInfo>) null;

        try {
            var response = cloudWatchLogs.describeLogStreamsPaginator(requestBuilder.build());

            logStreams = response.logStreams()
                    .stream()
                    .map(x -> new LogStreamInfo(x.logStreamName(), x.creationTime(), x.firstEventTimestamp(),
                            x.lastEventTimestamp(), x.lastIngestionTime(), x.uploadSequenceToken()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9001, command.getLogGroupName());
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
