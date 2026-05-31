# StatHarbor API 接口文档

> Base URL: `https://udu.yituliu.cn/`
>
> Content-Type: `application/json`

---

## 1. 上传通用数据

### 接口信息

| 项目 | 值 |
|---|---|
| 方法 | `POST` |
| 路径 | `/v1/records` |
| 认证 | 可选（携带 Token 享受更高限额） |

### 认证方式

在请求头中携带 `Authorization`，Token 前缀为 `yituliu `（注意末尾空格）：

```
Authorization: yituliu <secret_key>
```

| 认证状态 | 未认证 | 已认证 |
|---|---|---|
| 请求体上限 | 2KB | 4KB |
| 每分钟请求数 | 20 | 120 |
| 每日记录数 | 1,000 | 100,000 |

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `projectKey` | String | ✅ | 统计项目标识，建议小写英文、数字、下划线、点号、短横线 |
| `category` | String | ✅ | 数据分类，如 `stage_drop`、`store` |
| `version` | String | ✅ | 数据结构版本，如 `v1` |
| `source` | String | ✅ | 数据来源，如 `ocr`、`log`、`api`、`manual` |
| `capturedAt` | String | ❌ | 采集时间的毫秒时间戳，如 `1748736000000` |
| `note` | String | ❌ | 备注，适合记录异常、置信度说明等 |
| `payload` | Object | ✅ | 原始 JSON 内容，内部字段暂不限制 |

### 请求示例

**未认证请求：**

```json
{
    "projectKey": "my_game_main",
    "category": "stage_drop",
    "version": "v1",
    "source": "ocr",
    "capturedAt": "1748736000000",
    "payload": {
        "account_id": "main",
        "stage": "A-12",
        "score": 12800,
        "is_win": true
    }
}
```

**已认证请求（带 note）：**

```json
{
    "projectKey": "my_game_main",
    "category": "stage_drop",
    "version": "v1",
    "source": "ocr",
    "capturedAt": "1748736000000",
    "note": "来自 OCR 识别",
    "payload": {
        "account_id": "main",
        "stage": "A-12",
        "score": 12800,
        "is_win": true
    }
}
```

**curl 示例：**

```bash
# 未认证
curl -X POST https://udu.yituliu.cn/v1/records \
  -H "Content-Type: application/json" \
  -d '{"projectKey":"my_game_main","category":"stage_drop","version":"v1","source":"ocr","capturedAt":"1748736000000","payload":{"account_id":"main","score":12800,"is_win":true}}'

# 已认证
curl -X POST https://udu.yituliu.cn/v1/records \
  -H "Content-Type: application/json" \
  -H "Authorization: yituliu <your-secret-key>" \
  -d '{"projectKey":"my_game_main","category":"stage_drop","version":"v1","source":"ocr","capturedAt":"1748736000000","note":"来自 OCR","payload":{"account_id":"main","score":12800,"is_win":true}}'
```

### 响应格式

所有接口统一使用以下格式返回：

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": null
}
```

### 成功响应

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": "上传成功"
}
```

### 错误响应

| code | 说明 |
|---|---|
| 10001 | 参数无效 |
| 10005 | 含有非法参数，请检查上传内容 |
| 10009 | Payload 不能为空 |
| 10010 | ProjectKey 不能为空 |
| 10011 | Category 不能为空 |
| 10012 | Version 不能为空 |
| 10013 | Source 不能为空 |
| 60006 | 请求过于频繁 |
| 60008 | 请求体过大，未认证状态下不得超过 4KB |
| 60009 | 每日记录数已达上限 |

示例：

```json
{
    "code": 10009,
    "msg": "Payload不能为空",
    "data": null
}
```

---

## 附：数据流说明

```
采集器 → POST /v1/records
        │
        ▼
  ApiAuthInterceptor（拦截器）
    ├─ ① 认证识别（Authorization 头解析 + Redis 校验）
    ├─ ② 每分钟限流检查
    ├─ ③ 每日记录数上限检查
    └─ ④ Payload 体量检查
        │
        ▼
  UniversalDataCollectionController
    └─ validateDTO（校验必填字段）
    └─ convertToEntity（DTO → Entity，时间戳转换）
    └─ universalDataMapper.insert（写入数据库）
        │
        ▼
  响应 { code: 200, msg: "操作成功", data: "上传成功" }
```
