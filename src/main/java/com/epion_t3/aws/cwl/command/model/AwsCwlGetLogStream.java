/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.model;

import com.epion_t3.aws.cwl.command.runner.AwsCwlGetLogStreamRunner;
import com.epion_t3.core.common.annotation.CommandDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CommandDefinition(id = "AwsCwlGetLogStream", runner = AwsCwlGetLogStreamRunner.class)
public class AwsCwlGetLogStream extends AwsCwlBase {
    private String logGroupName;
    private String logStreamNamePrefix;
    private Integer limit;
    private String orderBy;
    private boolean descending;
}
