/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.runner;

import com.epion_t3.aws.cwl.bean.AssertLogEventInfo;
import com.epion_t3.aws.cwl.bean.AssertLogEventResult;
import com.epion_t3.aws.cwl.bean.LogEventAssertDetail;
import com.epion_t3.aws.cwl.command.model.AwsCwlAssertLogEvents;
import com.epion_t3.aws.cwl.messages.AwsCwlMessages;
import com.epion_t3.core.command.bean.CommandResult;
import com.epion_t3.core.command.runner.impl.AbstractCommandRunner;
import com.epion_t3.core.common.type.AssertStatus;
import com.epion_t3.core.exception.SystemException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 */
public class AwsCwlAssertLogEventsRunner extends AbstractCommandRunner<AwsCwlAssertLogEvents> {

    /**
     * オブジェクトマッパー.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public CommandResult execute(AwsCwlAssertLogEvents command, Logger logger) throws Exception {

        // 結果の生成
        var commandResult = new AssertLogEventResult();

        // 結果値のパスを取得（エビデンスより）
        var actualPath = referFileEvidence(command.getActualFlowId());
        // 結果値のパスが存在しなかった場合はエラー
        if (Files.notExists(actualPath)) {
            throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9005, actualPath.toString(),
                    command.getActualFlowId());
        }
        // ファイル内容を読み込み
        var ctx = JsonPath.parse(actualPath.toFile());

        // 比較対象を抽出
        var actual = command.isJson() ?
        // 指定したJSONPathから結果値を抽出
                (List<String>) ctx.read(command.getTargetJsonPath()) :
                // LogEventInfoのmessageを結果値として抽出
                (List<String>) ctx.read("$.[*].message");

        // 期待値を確定
        var expected = (List<AssertLogEventInfo>) null;
        if (command.getExpected() != null) {
            // シナリオファイルに直接記載されている場合優先する
            expected = command.getExpected();
        } else if (StringUtils.isNotEmpty(command.getExpectedPath())) {
            // ファイルパスが指定されている場合
            var expectedFilePath = Paths.get(getScenarioDirectory(), command.getExpectedPath());

            // スクリプトパスが存在しなかった場合はエラー
            if (Files.notExists(expectedFilePath)) {
                throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9007, expectedFilePath.toString());
            }
            expected = objectMapper.readValue(Files.readString(expectedFilePath),
                    new TypeReference<List<AssertLogEventInfo>>() {
                    });
        } else {
            throw new SystemException(AwsCwlMessages.AWS_CWL_ERR_9006);
        }

        Objects.requireNonNull(expected).forEach(x -> {
            var logEventAssert = new LogEventAssertDetail();
            // 初期状態はアサートNGとしておく
            logEventAssert.setAssertStatus(AssertStatus.NG);
            if (x.getRegexp()) {
                // 正規表現である場合、正規表現とマッチするかを比較
                var pattern = Pattern.compile(x.getPattern());
                logEventAssert.setExpected(x.getPattern());
                logEventAssert.setRegexp(x.getRegexp());
                actual.forEach(y -> {
                    var matcher = pattern.matcher(y);
                    if (matcher.matches()) {
                        logEventAssert.setActual(y);
                        logEventAssert.setAssertStatus(AssertStatus.OK);
                    }
                });
            } else {
                // 正規表現んでない場合、そのまま比較
                actual.forEach(y -> {
                    if (StringUtils.equals(x.getPattern(), y)) {
                        logEventAssert.setActual(y);
                        logEventAssert.setAssertStatus(AssertStatus.OK);
                    }
                });
            }
            commandResult.add(logEventAssert);
        });
        commandResult.setAssertStatus(commandResult.hasAssertError() ? AssertStatus.NG : AssertStatus.OK);
        return commandResult;
    }
}
