# やること管理アプリ DB設計書

## 1. 文書の目的

本書は、やること管理アプリが使用するデータベースの構造（データの保存方法）を定めるものです。

## 2. 対象テーブル

アプリで使用するテーブルは `todos` の1つだけです。1件のやることを、`todos` の1行として保存します。

| 項目 | 内容 |
|---|---|
| テーブル名 | `todos` |
| 保存するもの | やること1件分の情報 |
| 主キー | `id` |

## 3. カラム定義

主キー（1行を一意に識別する番号）は `id` です。

| カラム名 | データ型 | NULL | 初期値 | 制約・役割 | 対応する要件 |
|---|---|---|---|---|---|
| `id` | `BIGINT` | 不可 | 自動採番 | 主キー、自動的に1ずつ増える | 1件を識別する番号。利用者は入力しない |
| `title` | `VARCHAR(255)` | 不可 | なし | やることの名前 | 必須。255文字以内。名前検索の対象 |
| `detail` | `VARCHAR(255)` | 可 | `NULL` | やることのメモ | 任意。255文字以内 |
| `category` | `VARCHAR(255)` | 不可 | なし | 指定された5種類のいずれか | 必須。ジャンル検索の対象 |
| `priority` | `INT` | 不可 | `2` | `1`、`2`、`3`で管理 | 1＝高、2＝中、3＝低。必須 |
| `due_date` | `DATE` | 可 | `NULL` | やることの期限 | 任意。期限順の並び替えに使用 |
| `completed` | `BOOLEAN` | 不可 | `FALSE` | 完了状態 | `TRUE`＝完了、`FALSE`＝未完了 |
| `created_at` | `DATETIME` | 不可 | 現在日時 | 登録時に自動設定 | 登録日時。利用者は入力しない |
| `updated_at` | `DATETIME` | 不可 | 現在日時 | 登録時・更新時に自動設定 | 最終更新日時。利用者は入力しない |

## 4. 許可する値

### 4.1 `category`

`category` に保存できる値は、次の5つだけです。

- `デザイン`
- `マーケティング`
- `プログラミング`
- `資格`
- `就職活動`

画面では自由入力ではなく、5つから選択します。

### 4.2 `priority`

| 保存値 | 画面に表示する値 |
|---:|---|
| `1` | 高 |
| `2` | 中 |
| `3` | 低 |

未指定の場合の初期値は `2`（中）です。ただし、登録画面では必須項目として選択させます。

### 4.3 `completed`

| 保存値 | 画面に表示する値 |
|---|---|
| `TRUE`（1） | 完了 |
| `FALSE`（0） | 未完了 |

初期値は `FALSE`（未完了）です。

## 5. 自動設定するカラム

次のカラムは利用者の入力欄に表示せず、データベースが値を設定します。

- `id`：新しい行を追加するときに自動採番します。
- `created_at`：行を追加した時点の日時を設定します。
- `updated_at`：行を追加した時点の日時を設定し、行を更新したときに更新日時へ変更します。

## 6. 要件とカラムの対応

| 要件 | 対応するカラム |
|---|---|
| やることを登録する | `title`, `detail`, `category`, `priority`, `due_date` |
| 登録したやることを一覧表示する | `id`, `title`, `category`, `priority`, `due_date`, `completed` |
| やることを編集する | `title`, `detail`, `category`, `priority`, `due_date`, `completed` |
| やることを削除する | `id` |
| やることの名前で一部一致検索する | `title` |
| ジャンルで完全一致検索する | `category` |
| 期限が近い順・遠い順に並び替える | `due_date` |
| 完了・未完了を表示する | `completed` |
| 一覧画面で完了・未完了を切り替える | `completed` |
| 登録日時を保存する | `created_at` |
| 最終更新日時を保存する | `updated_at` |
| 操作記録に対象の番号を残す | `id` |

## 7. CREATE TABLE文（DDL）

DDL（Data Definition Language：テーブルを作るためのSQL文）は次のとおりです。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category CHECK (
        category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')
    ),
    CONSTRAINT chk_todos_priority CHECK (priority IN (1, 2, 3))
);
```

## 8. データベース設計上の補足

- `title` と `detail` の最大255文字、`category` の5種類、`priority` の3種類は、仕様書と要件定義書に基づく制約です。
- `CHECK` 制約（保存できる値を制限する仕組み）で、ジャンルと優先度が指定外の値にならないようにします。
- 主キー以外のインデックス（検索を速くするための仕組み）は、今回の仕様では追加しません。
- 上記の `CHECK` 制約や自動日時設定は、仕様にある入力値と自動設定をデータベース側でも守るための実装上の設定です。新しい画面機能や利用者向けの要件を追加するものではありません。

## 9. 操作記録の保存先

登録・編集・削除の操作記録は、データベースのテーブルには保存しません。アプリケーションのログ（アプリが出力する記録）として、操作の種類と対象の `id` を1行ずつ記録します。

`title` と `detail` の内容は、操作記録に残しません。
