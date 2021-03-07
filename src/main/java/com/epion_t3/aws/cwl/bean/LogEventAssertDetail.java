/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.bean;

import com.epion_t3.core.common.type.AssertStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class LogEventAssertDetail implements Serializable {

    /**
     * default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 期待値.
     */
    private String expected;

    /**
     * 正規表現.
     */
    private Boolean regexp;

    /**
     * 結果値.
     */
    private String actual;

    /**
     * アサート結果.
     */
    private AssertStatus assertStatus;
}
