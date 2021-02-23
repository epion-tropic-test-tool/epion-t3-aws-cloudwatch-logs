/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.model;

import com.epion_t3.aws.cwl.command.runner.AwsCwlGetLogStreamEventsRunner;
import com.epion_t3.core.common.annotation.CommandDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CommandDefinition(id = "AwsCwlGetLastLogStreamEvents", runner = AwsCwlGetLogStreamEventsRunner.class)
public class AwsCwlGetLastLogStreamEvents extends AwsCwlBase {
    private String logGroupName;
    private String logStreamNamePrefix;
    private String orderBy;
    private boolean descending;
    private Integer eventsLimit;
    private Long startTime;
    private Long endTime;
    // Default : false
    private Boolean startFromHead;
}
