/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogStreamInfo implements Serializable {
    private String logStreamName;
    private Long creationTime;
    private Long firstEventTimestamp;
    private Long lastEventTimestamp;
    private Long lastIngestionTime;
    private String uploadSequenceToken;
}
