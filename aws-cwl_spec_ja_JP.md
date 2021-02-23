#  カスタム機能ドキュメント

このドキュメントは、 のカスタム機能が提供する、
Flow、コマンド、設定定義についての説明及び出力するメッセージの定義について説明する。

- Contents
  - [Information](#Information)
  - [Description](#Description)
  - [Flow List](#Flow-List)
  - [Command List](#Command-List)
  - [Configuration List](#Configuration-List)
  - [Message List](#Message-List)

## Information

本カスタム機能の基本情報は以下の通り。

AWSのCloudWatchLogsへのアクセスを行う機能を提供します。

- Name : `aws-cwl`
- Custom Package : `com.epion_t3.aws.cwl`

## Description
AmazonWebService（AWS）のCloudWatchLogs（CWL）への各種アクセスを行う機能を提供します。エビデンスの取得などに利用できるGetLogStream、GetLogEventsを代表とした機能です。

## Flow List

本カスタム機能が提供するFlowの一覧及び詳細。

|Name|Summary|
|:---|:---|


## Command List

本カスタム機能が提供するコマンドの一覧及び詳細。

|Name|Summary|Assert|Evidence|
|:---|:---|:---|:---|
|[AwsCwlGetLogStream](#AwsCwlGetLogStream)|LogStreamを取得します。  ||X|
|[AwsCwlGetLogStreamEvents](#AwsCwlGetLogStreamEvents)|指定されたLogGroupの最終LogStreamのLogイベントを取得します。  ||X|
|[AwsCwlGetLogEvents](#AwsCwlGetLogEvents)|Logイベント（ログそのもの）を取得します。  ||X|

------

### AwsCwlGetLogStream
LogStreamを取得します。
#### Command Type
- Assert : No
- Evidence : __Yes__

#### Functions
- 指定されたLogGroupからLogStreamを取得します。
- logStreamNamePrefix、limit、orderBy、descendingによる取得制御が行えます。
- 取得したLogStreamの一覧は、JSON形式のエビデンスとして保存します。
- エビデンスファイル名は、「実行ID ＋ logStreams.json」となります。

#### Structure
```yaml
commands : 
  id : コマンドのID
  command : 「AwsCwlGetLogStream」固定
  summary : コマンドの概要（任意）
  description : コマンドの詳細（任意）
  logGroupName : 対象とするLogGroupの名前を指定します。
  logStreamNamePrefix : 対象とするLogStreamの接頭辞を指定します。
  limit : 取得するLogStreamの上限件数を指定します。
  orderBy : 結果のソート方法を「LogStreamName」、「LastEventTime」のいずれかを指定します。いずれかの文字列の完全一致でなければなりません。
  descending : 結果のソートを降順にするかどうかのフラグです。

```

------

### AwsCwlGetLogStreamEvents
指定されたLogGroupの最終LogStreamのLogイベントを取得します。
#### Command Type
- Assert : No
- Evidence : __Yes__

#### Functions
- 指定されたLogGroupの最終LogStreamのLogイベントを取得します。
- logStreamNamePrefix、limit、orderBy、descendingによるLogStream取得制御が行えます。
- eventsLimit、startTime、endTimeによるLogイベント取得制御が行えます。
- 取得したログの一覧は、JSON形式のエビデンスとして保存します。
- エビデンスファイル名は、「実行ID ＋ logEvents.json」となります。

#### Structure
```yaml
commands : 
  id : コマンドのID
  command : 「AwsCwlGetLogStreamEvents」固定
  summary : コマンドの概要（任意）
  description : コマンドの詳細（任意）
  logGroupName : 対象とするLogGroupの名前を指定します。
  logStreamNamePrefix : 対象とするLogStreamの接頭辞を指定します。
  streamLimit : LogStreamの件数の上限を設定します。デフォルトは1です。この数より多いLogStreamが存在する場合でも指定された値までしか取得しません。
  orderBy : 結果のソート方法を「LogStreamName」、「LastEventTime」のいずれかを指定します。いずれかの文字列の完全一致でなければなりません。
  descending : 結果のソートを降順にするかどうかのフラグです。
  eventLimit : 取得するLogイベントの上限件数を指定します。この数より多いLogイベントが存在する場合でも指定された値までしか取得しません。
  startTime : イベントの開始時間を指定します。エポックミリ秒で指定します。
  endTime : イベントの終了時間を指定します。エポックミリ秒で指定します。
  startFromHead : イベントの開始を先頭から出力するかのフラグです。デフォルトはfalseです。

```

------

### AwsCwlGetLogEvents
Logイベント（ログそのもの）を取得します。
#### Command Type
- Assert : No
- Evidence : __Yes__

#### Functions
- 指定されたLogGroup、LogStreamからLogイベント（ログそのもの）を取得します。
- limit、startTime、endTimeによる取得制御が行えます。
- 取得したログの一覧は、JSON形式のエビデンスとして保存します。
- エビデンスファイル名は、「実行ID ＋ logEvents.json」となります。

#### Structure
```yaml
commands : 
  id : コマンドのID
  command : 「AwsCwlGetLogEvents」固定
  summary : コマンドの概要（任意）
  description : コマンドの詳細（任意）
  logGroupName : 対象とするLogGroupの名前を指定します。
  logStreamName : 対象とするLogStreamの接頭辞を指定します。
  limit : 取得するLogStreamの上限件数を指定します。この数より多いLogStreamが存在する場合でも指定された値までしか取得しません。
  startTime : イベントの開始時間を指定します。エポックミリ秒で指定します。
  endTime : イベントの終了時間を指定します。エポックミリ秒で指定します。
  startFromHead : イベントの開始を先頭から出力するかのフラグです。デフォルトはfalseです。

```


## Configuration List

本カスタム機能が提供する設定定義の一覧及び詳細。

|Name|Summary|
|:---|:---|


## Message List

本カスタム機能が出力するメッセージの一覧及び内容。

|MessageID|MessageContents|
|:---|:---|
|com.epion_t3.aws.cwl.err.9002|CloudWatchLogsのLogEventの取得に失敗しました。LogGroup:{0}, LogStream:{1}|
|com.epion_t3.aws.cwl.err.9001|CloudWatchLogsのLogStreamの取得に失敗しました。LogGroup:{0}|
|com.epion_t3.aws.cwl.err.9003|合致するLogStreamが1件も見つかりません。LogGroup:{0}|
