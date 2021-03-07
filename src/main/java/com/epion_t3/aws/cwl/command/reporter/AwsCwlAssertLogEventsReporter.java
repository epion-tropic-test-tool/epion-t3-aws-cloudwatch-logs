/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.reporter;

import com.epion_t3.aws.cwl.bean.AssertLogEventResult;
import com.epion_t3.aws.cwl.command.model.AwsCwlAssertLogEvents;
import com.epion_t3.core.command.reporter.impl.AbstractThymeleafCommandReporter;
import com.epion_t3.core.common.bean.ExecuteCommand;
import com.epion_t3.core.common.bean.ExecuteFlow;
import com.epion_t3.core.common.bean.ExecuteScenario;
import com.epion_t3.core.common.context.ExecuteContext;

import java.util.Map;

/**
 * AwsCwlAssertLogEventsコマンドのカスタムレポートクラス.
 *
 * @author Nozomu.Takashima
 */
public class AwsCwlAssertLogEventsReporter
        extends AbstractThymeleafCommandReporter<AwsCwlAssertLogEvents, AssertLogEventResult> {

    /**
     * {@inheritDoc}}
     */
    @Override
    public String templatePath() {
        return "aws-cwl-assert-log-events-report";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVariables(Map<String, Object> variable, AwsCwlAssertLogEvents command,
            AssertLogEventResult assertCommandResult, ExecuteContext executeContext, ExecuteScenario executeScenario,
            ExecuteFlow executeFlow, ExecuteCommand executeCommand) {

        // コマンドの格納
        variable.put("command", command);

        // Assertion結果の詳細情報の格納
        variable.put("assertDetails", assertCommandResult.getLogEventAssertDetails());
    }

}
