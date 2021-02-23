/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.model;

import com.epion_t3.aws.cwl.command.runner.AwsCwlGetLogEventsRunner;
import com.epion_t3.core.common.annotation.CommandDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CommandDefinition(id = "AwsCwlAssertLogEvents", runner = AwsCwlGetLogEventsRunner.class)
public class AwsCwlAssertLogEvents extends AwsCwlBase {
}
