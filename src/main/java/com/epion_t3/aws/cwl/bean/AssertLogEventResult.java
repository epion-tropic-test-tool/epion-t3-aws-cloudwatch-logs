/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.bean;

import com.epion_t3.core.command.bean.AssertCommandResult;
import com.epion_t3.core.common.type.AssertStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AssertLogEventResult extends AssertCommandResult {

    private List<LogEventAssertDetail> logEventAssertDetails;

    public void add(LogEventAssertDetail assertDetail) {
        if (logEventAssertDetails == null) {
            logEventAssertDetails = new ArrayList<>();
        }
        logEventAssertDetails.add(assertDetail);
    }

    public boolean hasAssertError() {
        return logEventAssertDetails.stream().anyMatch(x -> x.getAssertStatus() == AssertStatus.NG);
    }
}
