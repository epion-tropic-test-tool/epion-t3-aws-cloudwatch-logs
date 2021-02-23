/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.messages;

import com.epion_t3.core.message.Messages;

/**
 * aws-cwl用メッセージ定義Enum.<br>
 *
 * @author epion-t3-devtools
 */
public enum AwsCwlMessages implements Messages {

    /** CloudWatchLogsのLogEventの取得に失敗しました。LogGroup:{0}, LogStream:{1} */
    AWS_CWL_ERR_9002("com.epion_t3.aws.cwl.err.9002"),

    /** CloudWatchLogsのLogStreamの取得に失敗しました。LogGroup:{0} */
    AWS_CWL_ERR_9001("com.epion_t3.aws.cwl.err.9001"),

    /** 合致するLogStreamが1件も見つかりません。LogGroup:{0} */
    AWS_CWL_ERR_9003("com.epion_t3.aws.cwl.err.9003");

    /** メッセージコード */
    private String messageCode;

    /**
     * プライベートコンストラクタ<br>
     *
     * @param messageCode メッセージコード
     */
    private AwsCwlMessages(final String messageCode) {
        this.messageCode = messageCode;
    }

    /**
     * messageCodeを取得します.<br>
     *
     * @return messageCode
     */
    public String getMessageCode() {
        return this.messageCode;
    }
}
