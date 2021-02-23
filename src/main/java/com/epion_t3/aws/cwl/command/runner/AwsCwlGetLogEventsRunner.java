/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.runner;

import com.epion_t3.aws.core.configuration.AwsCredentialsProviderConfiguration;
import com.epion_t3.aws.core.holder.AwsCredentialsProviderHolder;
import com.epion_t3.aws.cwl.command.model.AwsCwlGetLogEvents;
import com.epion_t3.aws.cwl.command.model.LogEventInfo;
import com.epion_t3.aws.cwl.messages.AwsCwlMessages;
import com.epion_t3.core.command.bean.CommandResult;
import com.epion_t3.core.command.runner.impl.AbstractCommandRunner;
import com.epion_t3.core.exception.SystemException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AwsCwlGetLogEventsRunner extends AbstractCommandRunner<AwsCwlGetLogEvents> {

    /**
     * オブジェクトマッパー.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResult execute(AwsCwlGetLogEvents command, Logger logger) throws Exception {

        var awsCredentialsProviderConfiguration = (AwsCredentialsProviderConfiguration) referConfiguration(
                command.getCredentialsConfigRef());

        var credentialsProvider = AwsCredentialsProviderHolder.getInstance()
                .getCredentialsProvider(awsCredentialsProviderConfiguration);

        var cloudWatchLogs = CloudWatchLogsClient.builder().credentialsProvider(credentialsProvider).build();

        var requestBuilder = GetLogEventsRequest.builder()
                .logGroupName(command.getLogGroupName())
                .logStreamName(command.getLogStreamName());

        if (command.getLimit() != null) {
            requestBuilder.limit(command.getLimit());
        }

        if (command.getStartTime() != null) {
            requestBuilder.startTime(command.getStartTime());
        }

        if (command.getEndTime() != null) {
            requestBuilder.endTime(command.getEndTime());
        }

        if (command.getStartFromHead() != null) {
            requestBuilder.startFromHead(command.getStartFromHead());
        }

        var logEvents = (List<LogEventInfo>) null;
        try {
            var response = cloudWatchLogs.getLogEventsPaginator(requestBuilder.build());
            logEvents = response.events().stream().map(x -> {
                try {
                    return new LogEventInfo(x.ingestionTime(), x.timestamp(), x.message(),
                            objectMapper.readValue(x.message(), (new LinkedHashMap<String, Object>(0)).getClass()));
                } catch (JsonProcessingException e) {
                    throw new SystemException();
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9002, command.getLogGroupName(),
                    command.getLogStreamName());
        }

        var evidencePath = getEvidencePath("logEvents.json");

        // ファイルエビデンス書き出し
        try (var fos = new FileOutputStream(evidencePath.toFile())) {
            objectMapper.writeValue(fos, logEvents);
        }

        // オブジェクトエビデンス登録
        registrationObjectEvidence(logEvents);

        // ファイルエビデンス登録
        registrationFileEvidence(evidencePath);

        return CommandResult.getSuccess();
    }

}
