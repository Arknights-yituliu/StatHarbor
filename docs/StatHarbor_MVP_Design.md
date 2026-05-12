# StatHarbor MVP 设计草稿

## 1. 项目定位

**StatHarbor** 是一个轻量级数据收集后端，用来接收自动化工具采集到的游戏统计数据。

第一版只负责：

- 接收 JSON 数据。
- 按项目和分类归档。
- 保存原始 `payload`。
- 控制带宽和存储成本。
- 提供基础查询。

第一版不负责：

- 清洗数据。
- 统一不同分类的字段结构。
- 做统计分析。
- 理解 `payload` 内部业务含义。

## 2. 核心设计

外层字段用于管理、查询和限制；真实采集内容全部放进 `payload`。

采集器提交：

```json
{
  "project_key": "my_game_main",
  "category": "arena.daily_reward",
  "captured_at": "2026-05-12T10:30:00Z",
  "payload": {
    "account_id": "main",
    "rank": 128,
    "reward_claimed": true
  }
}
```

后端保存：

```json
{
  "id": "b89f0f38-12e0-437f-97df-59cc6f6a181d",
  "project_key": "my_game_main",
  "category": "arena.daily_reward",
  "version": "v1",
  "source": "ocr",
  "captured_at": "2026-05-12T10:30:00Z",
  "received_at": "2026-05-12T10:30:03Z",
  "payload_size_bytes": 84,
  "note": "OCR 结果已人工确认",
  "payload": {
    "account_id": "main",
    "rank": 128,
    "reward_claimed": true
  }
}
```

## 3. 字段初稿

| 字段 | 类型 | 必填 | 来源 | 说明 |
|---|---|---:|---|---|
| `id` | `string` | 是 | 后端 | 记录唯一 ID，建议 UUID。 |
| `project_key` | `string` | 是 | 采集器 | 统计项目标识。 |
| `category` | `string` | 是 | 采集器 | 数据分类，开放字符串，不做硬枚举。 |
| `version` | `string` | 是 | 采集器 | 数据结构版本，例如 `v1`。 |
| `source` | `string` | 是 | 采集器 | 数据来源，例如 `ocr`、`log`、`api`、`manual`。 |
| `captured_at` | `string` | 是 | 采集器 | 采集时间，ISO 8601 格式。 |
| `received_at` | `string` | 是 | 后端 | 后端收到时间，ISO 8601 格式。 |
| `payload_size_bytes` | `number` | 是 | 后端 | `payload` 字节大小。 |
| `note` | `string` | 否 | 采集器 | 备注，适合记录异常、置信度说明等。 |
| `payload` | `object` | 是 | 采集器 | 原始 JSON 内容。 |

命名规则：

- `project_key` 和 `category` 建议只使用小写英文、数字、下划线、点号、短横线。
- `project_key` 和 `category` 建议限制在 `1-128` 个字符。
- `version` 和 `source` 必须由采集器提交。
- `note` 是可选元信息；如果暂时用不上，可以不传。
- `payload` 必须是 JSON 对象，内部字段暂不限制。

## 4. 项目类型

StatHarbor 分为两种项目：

| 类型 | 说明 |
|---|---|
| 未注册项目 | 没有提前创建的 `project_key`，允许无 Token 提交，但限制严格。 |
| 已注册项目 | 系统中正式创建并绑定 Token 的项目，限制更宽。 |

MVP 阶段允许完全无 Token 的请求。无 Token 请求只能使用未注册项目限制，后端通过内容长度、请求频率、每日体积等规则控制成本和风险。

放宽限制时应根据 `project_key + API Token` 判断，不能只看 `category`。

未注册项目不需要公共 Token。

## 5. 限制策略

初始建议：

| 限制项 | 未注册项目 | 已注册项目 |
|---|---:|---:|
| 单条 `payload` 最大大小 | `64 KB` | `1 MB` |
| 单次批量最多记录数 | `20` | `500` |
| 每分钟请求数 | `20` | `120` |
| 每日最多记录数 | `1,000` | `100,000` |
| 每日 `payload` 总体积 | `10 MB` | `1 GB` |
| 数据保留时间 | 暂不确定 | 暂不确定 |
| 单项目最多 `category` 数量 | `10` | `100+` |

这些值只是 MVP 起点，后续可以根据真实成本调整。

数据是否自动删除先留档，暂不作为 MVP 必须决策。

## 6. API 初稿

提交单条记录：

```http
POST /api/v1/records
Authorization: Bearer <api_token>
Content-Type: application/json
```

`Authorization` 可选。未携带 Token 时，按未注册项目限制处理。

请求体：

```json
{
  "project_key": "my_game_main",
  "category": "battle_result",
  "version": "v1",
  "source": "ocr",
  "captured_at": "2026-05-12T10:30:00Z",
  "note": "OCR 结果已人工确认",
  "payload": {
    "score": 12800,
    "is_win": true
  }
}
```

响应体：

```json
{
  "id": "b89f0f38-12e0-437f-97df-59cc6f6a181d",
  "received_at": "2026-05-12T10:30:03Z",
  "payload_size_bytes": 37
}
```

批量提交：

```http
POST /api/v1/records/batch
```

请求体：

```json
{
  "records": []
}
```

## 7. 存储初稿

MVP 阶段可以先用一张核心表：`records`。

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | UUID | 主键。 |
| `project_key` | TEXT | 建议加索引。 |
| `category` | TEXT | 建议加索引。 |
| `version` | TEXT | 可选，数据结构版本。 |
| `source` | TEXT | 可选，数据来源。 |
| `captured_at` | TIMESTAMP | 建议加索引。 |
| `received_at` | TIMESTAMP | 建议加索引。 |
| `payload_size_bytes` | INTEGER | 用于额度统计。 |
| `note` | TEXT | 可选，备注。 |
| `payload` | JSONB | 原始 JSON 数据。 |

后续可以增加 `projects` 表，用来保存注册项目、Token、保留时间和自定义限制。

## 8. MVP 范围

第一版建议做：

- 单条 JSON 提交。
- 批量 JSON 提交。
- 允许无 Token 提交。
- 外层字段校验。
- `payload` 大小计算。
- 未注册和已注册项目的不同限制。
- 按 `project_key`、`category`、时间范围查询。

第一版先不做：

- 复杂用户系统。
- 可视化仪表盘。
- `payload` 内部字段校验。
- 自动统计分析。
- 消息队列。
- 非 JSON 数据格式。

## 9. 待确认问题

- 过期数据是否自动删除？
- 是否需要导出 JSON 文件？
- 项目注册先人工创建，还是做用户自助创建？

已确认：

- 允许完全无 Token 的请求。
- 未注册项目不需要公共 Token。
- 无 Token 请求仅通过内容长度、请求频率、每日体积等后端规则限制。
- 数据是否自动删除暂时留档，以后再决定。
