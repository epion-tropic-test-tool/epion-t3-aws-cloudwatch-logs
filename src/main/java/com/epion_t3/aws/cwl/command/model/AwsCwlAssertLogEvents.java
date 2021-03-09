/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.cwl.command.model;

import com.epion_t3.aws.cwl.bean.AssertLogEventInfo;
import com.epion_t3.aws.cwl.command.reporter.AwsCwlAssertLogEventsReporter;
import com.epion_t3.aws.cwl.command.runner.AwsCwlAssertLogEventsRunner;
import com.epion_t3.core.common.annotation.CommandDefinition;
import com.epion_t3.core.common.bean.scenario.Command;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@CommandDefinition(id = "AwsCwlAssertLogEvents", runner = AwsCwlAssertLogEventsRunner.class, reporter = AwsCwlAssertLogEventsReporter.class, assertCommand = true)
public class AwsCwlAssertLogEvents extends Command {

    /**
     * 結果値を取得したFlowID.
     */
    private String actualFlowId;

    /**
     * メッセージがJSONであるかどうか.
     */
    private boolean json = false;

    /**
     * アサート対象のメッセージを抽出するJSON Path.
     */
    private String targetJsonPath;

    /**
     * 期待値リスト.
     */
    private List<AssertLogEventInfo> expected;

    /**
     * 期待値リスト定義ファイルパス.
     */
    private String expectedPath;
}
