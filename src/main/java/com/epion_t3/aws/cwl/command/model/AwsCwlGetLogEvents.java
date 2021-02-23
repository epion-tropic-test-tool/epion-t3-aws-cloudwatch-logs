/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.model;

import com.epion_t3.aws.cwl.command.runner.AwsCwlGetLogEventsRunner;
import com.epion_t3.core.common.annotation.CommandDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CommandDefinition(id = "AwsCwlGetLogEvents", runner = AwsCwlGetLogEventsRunner.class)
public class AwsCwlGetLogEvents extends AwsCwlBase {
    private String logGroupName;
    private String logStreamName;
    private Integer limit;
    private Long startTime;
    private Long endTime;
    // Default : false
    private Boolean startFromHead;
    // メッセージがJSONであるかどうか
    private boolean json = false;
}
