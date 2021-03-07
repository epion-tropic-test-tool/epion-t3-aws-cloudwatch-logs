/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AssertLogEventInfo implements Serializable {

    /**
     * 確認パターン.
     */
    private String pattern;

    /**
     * 正規表現フラグ.
     */
    private Boolean regexp;

}
