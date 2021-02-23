/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LogEventsInfo {
    private String logStreamName;
    private Long creationTime;
    private Long firstEventTimestamp;
    private Long lastEventTimestamp;
    private Long lastIngestionTime;
    private String uploadSequenceToken;
    private List<LogEventInfo> events;
}
