/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.runner;

import com.epion_t3.aws.cwl.bean.AssertLogEventResult;
import com.epion_t3.aws.cwl.bean.LogEventAssertDetail;
import com.epion_t3.aws.cwl.command.model.AwsCwlAssertLogEvents;
import com.epion_t3.aws.cwl.messages.AwsCwlMessages;
import com.epion_t3.core.command.bean.CommandResult;
import com.epion_t3.core.command.runner.impl.AbstractCommandRunner;
import com.epion_t3.core.common.type.AssertStatus;
import com.epion_t3.core.exception.SystemException;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
public class AwsCwlAssertLogEventsRunner extends AbstractCommandRunner<AwsCwlAssertLogEvents> {
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

        // メッセージがJSONではない場合
        // 比較対象を抽出
        var actual = command.isJson() ?
        // 指定したJSONPathから結果値を抽出
                (List<String>) ctx.read(command.getTargetJsonPath()) :
                // LogEventInfoのmessageを結果値として抽出
                (List<String>) ctx.read("$.[*].message");
        command.getExpected().forEach(x -> {
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
