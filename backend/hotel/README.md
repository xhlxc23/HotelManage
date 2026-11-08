# Hotel Room Management 后端 API

酒店房间管理系统 Spring Boot 后端，提供用户端与管理员端两套 RESTful 接口。

- **Base URL**：`http://localhost:8080`
- **Java**：25（兼容 17+）
- **框架**：Spring Boot 4.1.2 + MyBatis-Plus 3.5.18
- **数据库**：MySQL，库名 `mydb`

---

## 通用响应结构

所有接口统一使用以下 JSON 包装：

```json
{
  "code": "200",
  "msg": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | String | `"200"` 表示成功，其他值表示失败 |
| msg | String | 提示信息 |
| data | T | 业务数据，类型随接口变化 |

**失败响应示例**：

```json
{
  "code": "400",
  "msg": "手机号格式错误",
  "data": null
}
```

---

## 枚举值速查

### roleType（角色类型）

| 值 | 说明 |
|----|------|
| 1 | 普通用户 |
| 2 | 管理员 |

### viplevel（VIP 等级）

| 值 | 说明 | 积分条件 |
|----|------|----------|
| 1 | 普通 | exp < 100 |
| 2 | 银卡 | 100 ≤ exp < 1000 |
| 3 | 金卡 | 1000 ≤ exp < 5000 |
| 4 | 大师 | exp ≥ 5000 |

### orders.status（订单状态）

| 值 | 说明 |
|----|------|
| 0 | 待确认 |
| 1 | 已确认未入住 |
| 2 | 已入住未退房 |
| 3 | 已退房完成 |
| 4 | 已取消 |
| 5 | 已拒绝 |
| 6 | 已过期 |

### room.status（房间运营状态）

| 值 | 说明 |
|----|------|
| 0 | 停用/维修 |
| 1 | 可运营 |

---

## 目录

- [测试接口](#测试接口)
- [VR 全景接口 `/api/vr`](#vr-全景接口-apivr)
- [用户端接口 `/api/user`](#用户端接口-apiuser)
  - [用户登录](#1-用户登录)
  - [用户注册](#2-用户注册)
  - [获取用户信息](#3-获取用户信息)
  - [修改用户信息](#4-修改用户信息)
  - [查询可预订房间](#5-查询可预订房间)
  - [查询房间详情](#6-查询房间详情)
  - [查询历史订单](#7-查询历史订单)
  - [用户下单](#8-用户下单)
  - [用户修改订单状态（取消 / 确认）](#9-用户修改订单状态取消--确认)
- [管理员端接口 `/api/admin`](#管理员端接口-apiadmin)
  - [管理员登录](#1-管理员登录)
  - [根据管理员id获取管理员信息](#2-根据管理员id获取管理员信息)
  - [新建房型](#3-新建房型)
  - [修改房型](#4-修改房型)
  - [删除房型](#5-删除房型)
  - [查看所有房型](#6-查看所有房型)
  - [新增房间](#7-新增房间)
  - [设置房间状态](#8-设置房间状态)
  - [修改房间房型](#9-修改房间房型)
  - [查看所有房间（简化，含运营状态）](#10-查看所有房间简化含运营状态)
  - [查看所有房间（详细）](#11-查看所有房间详细)
  - [查看房间占用情况](#12-查看房间占用情况)
  - [查看所有订单（支持筛选）](#13-查看所有订单支持筛选)
  - [查询普通用户列表](#14-查询普通用户列表)
  - [修改订单状态](#15-修改订单状态)

---

## 测试接口

### GET /api/test

健康检查，返回 `"hello world"`。

**响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": "hello world"
}
```

---

## VR 全景接口 `/api/vr`

### GET /api/vr

根据房型 ID 获取该房型的 VR 全景数据，包含所有全景节点和节点间的跳转链接。

- **URL**：`GET /api/vr`
- **Query 参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | String | ✅ | 房型 ID（如 `C002`） |

**请求示例**：

```
GET /api/vr?categoryId=C002
```

**成功响应示例**（已配置 VR）：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "startNodeId": "p_c002_1",
    "nodes": [
      {
        "id": "p_c002_1",
        "panorama": "http://localhost:9000/picture/mo/bedroom.jpg",
        "name": "豪华大床房-主卧",
        "links": [
          {
            "nodeId": "p_c002_2",
            "label": "去卫浴",
            "position": {
              "yaw": "45deg",
              "pitch": "-20deg"
            }
          }
        ]
      },
      {
        "id": "p_c002_2",
        "panorama": "http://localhost:9000/picture/mo/bathroom.jpg",
        "name": "豪华大床房-卫浴",
        "links": [
          {
            "nodeId": "p_c002_1",
            "label": "回主卧",
            "position": {
              "yaw": "-135deg",
              "pitch": "-20deg"
            }
          }
        ]
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| startNodeId | String | 起始全景节点 ID，前端进入 VR 时从此节点开始展示 |
| nodes | List\<VrNode\> | 该房型所有全景节点列表 |

**VrNode 结构**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 节点 ID |
| panorama | String | 全景图片 URL |
| name | String | 节点名称 |
| links | List\<VrLink\> | 从该节点出发的跳转链接列表 |

**VrLink 结构**：

| 字段 | 类型 | 说明 |
|------|------|------|
| nodeId | String | 目标节点 ID |
| label | String | 链接显示文字（如"去卫浴"） |
| position | VrPosition | 热点在 3D 空间中的位置 |

**VrPosition 结构**：

| 字段 | 类型 | 说明 |
|------|------|------|
| yaw | String | 水平偏转角（如 `"45deg"`，数据库存储为 DECIMAL，返回时自动拼接 `deg`） |
| pitch | String | 垂直俯仰角（如 `"-20deg"`，数据库存储为 DECIMAL，返回时自动拼接 `deg`） |

**未配置 VR 时的响应**（该房型没有全景数据）：

```json
{
  "code": "404",
  "msg": "该房型尚未配置VR全景",
  "data": null
}
```

> 前端应判断 `code` 是否为 `"200"`，若为 `"404"` 则展示"该房型暂未提供 VR 看房"的提示。

---

## 用户端接口 `/api/user`

### 1. 用户登录

- **URL**：`POST /api/user/login`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| telephone | String | ✅ | 11 位手机号 |
| password | String | ✅ | 密码，6~20 位 |

**请求示例**：

```json
{
  "telephone": "13812345678",
  "password": "abc123456"
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "roleType": 1,
    "userTnfo": {
      "id": "1892345678901234567",
      "username": "小明",
      "viplevel": 2,
      "telephone": "13812345678",
      "exp": 320
    }
  }
}
```

---

### 2. 用户注册

- **URL**：`POST /api/user/register`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| telephone | String | ✅ | 11 位手机号，格式 `^1[3-9]\d{9}$` |
| password | String | ✅ | 6~20 位，仅字母/数字/下划线 |
| username | String | ✅ | 1~20 个字符 |

**请求示例**：

```json
{
  "telephone": "13987654321",
  "password": "my_pass_001",
  "username": "旅行达人"
}
```

**成功响应示例**（同用户登录）：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "roleType": 1,
    "userTnfo": {
      "id": "1892345678901234568",
      "username": "旅行达人",
      "viplevel": 1,
      "telephone": "13987654321",
      "exp": 0
    }
  }
}
```

---

### 3. 获取用户信息

- **URL**：`GET /api/user/userInfo`
- **Query 参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 用户 id |

**请求示例**：

```
GET /api/user/userInfo?id=1892345678901234567
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "id": "1892345678901234567",
    "username": "小明",
    "viplevel": 2,
    "telephone": "13812345678",
    "exp": 320
  }
}
```

---

### 4. 修改用户信息

仅 `id` 必填，其余字段为可选；**传入的非空字段才会被更新**，为空/null 的字段保留原值。

- **URL**：`POST /api/user/updateUserInfo`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 用户 id |
| username | String | ❌ | 1~20 个字符 |
| telephone | String | ❌ | 11 位手机号（若填写需符合格式） |
| password | String | ❌ | 6~20 位，直接覆盖原密码（不校验旧密码） |

**请求示例 1**：只改手机号

```json
{
  "id": "1892345678901234567",
  "telephone": "13812345678"
}
```

**请求示例 2**：只改密码

```json
{
  "id": "1892345678901234567",
  "password": "new_pass_001"
}
```

**请求示例 3**：修改全部

```json
{
  "id": "1892345678901234567",
  "username": "小明同学",
  "telephone": "13812345678",
  "password": "new_pass_001"
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "roleType": 1,
    "userTnfo": {
      "id": "1892345678901234567",
      "username": "小明同学",
      "viplevel": 2,
      "telephone": "13812345678",
      "exp": 320
    }
  }
}
```

---

### 5. 查询可预订房间

根据用户 id 和入住/退房时间，返回所有可用房间（排除已被占用的）。

- **URL**：`POST /api/user/availableRooms`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | ✅ | 用户 id |
| startTime | LocalDateTime | ✅ | 入住开始时间 |
| endTime | LocalDateTime | ✅ | 退房时间（必须晚于 startTime） |

**请求示例**：

```json
{
  "userId": "1892345678901234567",
  "startTime": "2026-11-20T14:00:00",
  "endTime": "2026-11-22T12:00:00"
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": [
    {
      "id": "1892345678901234101",
      "roomName": "301",
      "categoryId": "1892345678901234001",
      "categoryName": "豪华大床房",
      "picture": "https://example.com/images/deluxe-bed/room1.jpg",
      "description": "50㎡ 豪华大床房，配有落地窗和 2 米大床",
      "oneNightPrice": 588.00,
      "priceAfterDiscount": 529.20
    },
    {
      "id": "1892345678901234105",
      "roomName": "502",
      "categoryId": "1892345678901234002",
      "categoryName": "行政套房",
      "picture": "https://example.com/images/executive-suite/room1.jpg",
      "description": "80㎡ 行政套房，含独立客厅和行政酒廊权益",
      "oneNightPrice": 988.00,
      "priceAfterDiscount": 889.20
    }
  ]
}
```

---

### 6. 查询房间详情

- **URL**：`GET /api/user/oneRoomDetail`
- **Query 参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | ✅ | 用户 id（用于计算折扣价） |
| roomId | String | ✅ | 房间 id |

**请求示例**：

```
GET /api/user/oneRoomDetail?userId=1892345678901234567&roomId=1892345678901234101
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "id": "1892345678901234101",
    "roomName": "301",
    "categoryId": "1892345678901234001",
    "categoryName": "豪华大床房",
    "pictures": [
      "https://example.com/images/deluxe-bed/room1.jpg",
      "https://example.com/images/deluxe-bed/room2.jpg",
      "https://example.com/images/deluxe-bed/room3.jpg"
    ],
    "basePrice": 588.00,
    "priceAfterDiscount": 529.20,
    "description": "50㎡ 豪华大床房，配有落地窗和 2 米大床"
  }
}
```

---

### 7. 查询历史订单

- **URL**：`GET /api/user/historyOrders`
- **Query 参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 用户 id |

**请求示例**：

```
GET /api/user/historyOrders?id=1892345678901234567
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": [
    {
      "orderId": "1892345678901235001",
      "userId": "1892345678901234567",
      "userName": "小明同学",
      "userTelephone": "13800000001",
      "roomId": "1892345678901234101",
      "roomName": "301",
      "categoryName": "豪华大床房",
      "status": 3,
      "orderTime": "2026-11-10T20:15:30",
      "checkInTime": "2026-11-15T14:00:00",
      "checkOutTime": "2026-11-17T12:00:00",
      "cancelTime": null,
      "actualStartTime": "2026-11-15T14:30:00",
      "actualEndTime": "2026-11-17T11:45:00",
      "totalPrice": 1058.40,
      "viplevel": 2,
      "remark": "需要无烟房"
    },
    {
      "orderId": "1892345678901235002",
      "userId": "1892345678901234567",
      "userName": "小明同学",
      "userTelephone": "13800000001",
      "roomId": "1892345678901234105",
      "roomName": "502",
      "categoryName": "行政套房",
      "status": 1,
      "orderTime": "2026-11-05T10:00:00",
      "checkInTime": "2026-11-25T14:00:00",
      "checkOutTime": "2026-11-27T12:00:00",
      "cancelTime": null,
      "actualStartTime": null,
      "actualEndTime": null,
      "totalPrice": 1778.40,
      "viplevel": 2,
      "remark": null
    }
  ]
}
```

---

### 8. 用户下单

- **URL**：`POST /api/user/userSetOrder`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| user_accountId | String | ✅ | 用户 id |
| roomId | String | ✅ | 房间 id（必须可运营且目标时段未被占用） |
| orderTime | LocalDateTime | ✅ | 下单时间 |
| startTime | LocalDateTime | ✅ | 预订入住时间 |
| endTime | LocalDateTime | ✅ | 预订退房时间 |
| price | BigDecimal | ✅ | 最终结算价 |
| remark | String | ❌ | 客人备注 |

> **注**：`vipLevelSnapshot`（下单时会员等级快照）由后端从 `user_account.viplevel` 自动查询写入，前端无需传入。

**请求示例**：

```json
{
  "user_accountId": "1892345678901234567",
  "roomId": "1892345678901234101",
  "orderTime": "2026-11-06T10:30:00",
  "startTime": "2026-11-20T14:00:00",
  "endTime": "2026-11-22T12:00:00",
  "price": 1058.40,
  "remark": "需要无烟房，带早餐"
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "orderId": "1892345678901235100",
    "userId": "1892345678901234567",
    "userName": "小明同学",
    "userTelephone": "13800000001",
    "roomId": "1892345678901234101",
    "roomName": "301",
    "categoryName": "豪华大床房",
    "status": 0,
    "orderTime": "2026-11-06T10:30:00",
    "checkInTime": "2026-11-20T14:00:00",
    "checkOutTime": "2026-11-22T12:00:00",
    "cancelTime": null,
    "actualStartTime": null,
    "actualEndTime": null,
    "totalPrice": 1058.40,
    "viplevel": 2,
    "remark": "需要无烟房，带早餐"
  }
}
```

- **业务校验**：用户必须存在、房间必须可运营、时间段不重叠

---

### 9. 用户修改订单状态（取消 / 确认）

- **URL**：`POST /api/user/cancelOrder`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 订单 id |
| status | Integer | ✅ | 目标状态，用户侧只允许 `1`（已确认）或 `4`（已取消） |

**业务校验**：
- 订单必须存在
- **只有"待确认"状态（0）的订单**才能由用户操作

**请求示例（取消订单）**：

```json
{
  "id": "1892345678901235100",
  "status": 4
}
```

**请求示例（确认订单）**：

```json
{
  "id": "1892345678901235100",
  "status": 1
}
```

**成功响应示例**（data 为空）：

```json
{
  "code": "200",
  "msg": "success",
  "data": null
}
```

- **副作用**：当 `status = 4`（取消）时，自动记录取消时间并释放对应房间占用记录；`status = 1`（确认）无额外副作用

---

## 管理员端接口 `/api/admin`

### 1. 管理员登录

- **URL**：`POST /api/admin/login`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| telephone | String | ✅ | 11 位管理员手机号 |
| password | String | ✅ | 密码，6~20 位 |

**请求示例**：

```json
{
  "telephone": "15900001111",
  "password": "admin123"
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "roleType": 2,
    "adminTnfo": {
      "id": "1892345678901239001",
      "telephone": "15900001111"
    }
  }
}
```

---

### 2. 根据管理员id获取管理员信息

- **URL**：`GET /api/admin/info`

**请求参数**（Query）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 管理员 id |

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "id": "1892345678901239001",
    "adminname": "王管理员",
    "telephone": "15900001111"
  }
}
```

**业务校验**：管理员 id 必须存在，否则返回业务异常

---

### 3. 新建房型

- **URL**：`POST /api/admin/addcategory`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | ✅ | 房型名（唯一，如"豪华大床房"） |
| basicPrice | BigDecimal | ✅ | 每晚基础价格，≥ 0 |
| discounts | List\<Discount\> | ✅ | 各 VIP 等级折扣（1-4 级） |
| picture | List\<String\> | ❌ | 图片地址数组 |
| description | String | ❌ | 房型介绍 |

其中 `Discount` 结构：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| viplevel | Integer | ✅ | VIP 等级：1普通、2银卡、3金卡、4大师 |
| vipDiscount | BigDecimal | ✅ | 打折幅度，如 0.900 表示 9 折；普通用户(1级)应为 1.000 |

> 说明：`maxNumber` 不再由前端传入，改为由后端根据房间启用情况自动维护（默认 0，房间启用 +1 / 停用 -1）。

**请求示例**：

```json
{
  "name": "豪华大床房",
  "basicPrice": 588.00,
  "discounts": [
    { "viplevel": 1, "vipDiscount": 1.000 },
    { "viplevel": 2, "vipDiscount": 0.950 },
    { "viplevel": 3, "vipDiscount": 0.900 },
    { "viplevel": 4, "vipDiscount": 0.850 }
  ],
  "picture": [
    "https://example.com/images/deluxe-bed/room1.jpg",
    "https://example.com/images/deluxe-bed/room2.jpg"
  ],
  "description": "50㎡ 豪华大床房，配有落地窗和 2 米大床"
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": null
}
```

- **业务校验**：房型名不允许重复；折扣需提供 1-4 个 VIP 等级
- **副作用**：新建房型时 `maxnumber` 初始为 0，同写各 VIP 折扣到 `discount` 表

---

### 4. 修改房型

- **URL**：`POST /api/admin/updatecategory`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 房型 id（用于定位目标） |
| name | String | ❌ | 房型名（若修改需保证不重复） |
| basicPrice | BigDecimal | ❌ | 每晚基础价格 |
| discounts | List\<Discount\> | ❌ | 各 VIP 等级折扣（提供则覆盖式更新） |
| picture | List\<String\> | ❌ | 图片数组 |
| description | String | ❌ | 房型介绍 |

> 说明：`maxNumber` 由后端自动维护，不支持修改。

**请求示例**（仅修改价格）：

```json
{
  "id": "1892345678901234001",
  "basicPrice": 688.00
}
```

**请求示例**（修改价格并更新折扣）：

```json
{
  "id": "1892345678901234001",
  "basicPrice": 688.00,
  "discounts": [
    { "viplevel": 1, "vipDiscount": 1.000 },
    { "viplevel": 2, "vipDiscount": 0.930 },
    { "viplevel": 3, "vipDiscount": 0.880 },
    { "viplevel": 4, "vipDiscount": 0.820 }
  ]
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": null
}
```

- **注意**：传入的非空字段才会被更新

---

### 5. 删除房型

- **URL**：`POST /api/admin/deletecategory`
- **Query 参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 房型 id |

**请求示例**：

```
POST /api/admin/deletecategory?id=1892345678901234003
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": null
}
```

- **业务校验**：该房型下无房间引用才能删除；同时会级联删除关联的折扣配置

---

### 6. 查看所有房型

- **URL**：`GET /api/admin/getallcategory`

**请求示例**：

```
GET /api/admin/getallcategory
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": [
    {
      "id": "1892345678901234001",
      "name": "豪华大床房",
      "basicPrice": 688.00,
      "discounts": [
        { "viplevel": 1, "vipDiscount": 1.000 },
        { "viplevel": 2, "vipDiscount": 0.950 },
        { "viplevel": 3, "vipDiscount": 0.900 },
        { "viplevel": 4, "vipDiscount": 0.850 }
      ],
      "picture": [
        "https://example.com/images/deluxe-bed/room1.jpg",
        "https://example.com/images/deluxe-bed/room2.jpg"
      ],
      "description": "50㎡ 豪华大床房，配有落地窗和 2 米大床"
    },
    {
      "id": "1892345678901234002",
      "name": "行政套房",
      "basicPrice": 988.00,
      "discounts": [
        { "viplevel": 1, "vipDiscount": 1.000 },
        { "viplevel": 2, "vipDiscount": 0.950 },
        { "viplevel": 3, "vipDiscount": 0.900 },
        { "viplevel": 4, "vipDiscount": 0.850 }
      ],
      "picture": [
        "https://example.com/images/executive-suite/room1.jpg"
      ],
      "description": "80㎡ 行政套房，含独立客厅和行政酒廊权益"
    }
  ]
}
```

---

### 7. 新增房间

- **URL**：`POST /api/admin/addroom`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | ✅ | 房间名（如 301），≤ 10 字符 |
| categoryId | String | ✅ | 房型 id（必须已存在） |
| status | Integer | ❌ | 运营状态，空则默认 `1`（可运营） |

**请求示例**：

```json
{
  "name": "301",
  "categoryId": "1892345678901234001",
  "status": 1
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": null
}
```

---

### 8. 设置房间状态

- **URL**：`POST /api/admin/setroomstatus`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 房间 id |
| status | Integer | ✅ | 目标状态（0 停用 / 1 可运营） |

**请求示例**（停用房间）：

```json
{
  "id": "1892345678901234101",
  "status": 0
}
```

**请求示例**（启用房间）：

```json
{
  "id": "1892345678901234101",
  "status": 1
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": null
}
```

- **副作用**：同步更新所属房型的 `maxnumber`（停用 -1，启用 +1）

---

### 9. 修改房间房型

- **URL**：`POST /api/admin/updateroomcategory`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | ✅ | 房间 id |
| categoryId | String | ✅ | 目标房型 id |

**业务校验**：
- 房间必须存在
- 目标房型必须存在
- 若新旧房型相同，直接返回（空操作）

**请求示例**：

```json
{
  "id": "1892345678901234101",
  "categoryId": "1892345678901239002"
}
```

**成功响应示例**（data 为空）：

```json
{
  "code": "200",
  "msg": "success",
  "data": null
}
```

- **副作用**：若房间当前为"可运营"（status=1），自动修正新旧房型的 `maxnumber`（旧房型 -1，新房型 +1）；停用房间切换房型不影响 maxnumber

---

### 10. 查看所有房间（简化，含运营状态）

- **URL**：`GET /api/admin/rooms`

**请求示例**：

```
GET /api/admin/rooms
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": [
    {
      "id": "1892345678901234101",
      "roomName": "301",
      "status": 1,
      "categoryId": "1892345678901234001",
      "categoryName": "豪华大床房",
      "picture": "https://example.com/images/deluxe-bed/room1.jpg",
      "description": "50㎡ 豪华大床房，配有落地窗和 2 米大床"
    },
    {
      "id": "1892345678901234102",
      "roomName": "302",
      "status": 0,
      "categoryId": "1892345678901234001",
      "categoryName": "豪华大床房",
      "picture": "https://example.com/images/deluxe-bed/room1.jpg",
      "description": "50㎡ 豪华大床房，配有落地窗和 2 米大床"
    }
  ]
}
```

> 说明：`status` 取值 `1`=可运营、`0`=停用；该接口返回所有房间，不按运营状态过滤。该简化列表不含价格信息，需要详细数据请看"查看所有房间（详细）"。

---

### 11. 查看所有房间（详细）

- **URL**：`GET /api/admin/getallroom`

**请求示例**：

```
GET /api/admin/getallroom
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": [
    {
      "id": "1892345678901234101",
      "roomName": "301",
      "categoryId": "1892345678901234001",
      "categoryName": "豪华大床房",
      "pictures": [
        "https://example.com/images/deluxe-bed/room1.jpg",
        "https://example.com/images/deluxe-bed/room2.jpg"
      ],
      "basePrice": 688.00,
      "priceAfterDiscount": 688.00,
      "description": "50㎡ 豪华大床房，配有落地窗和 2 米大床"
    }
  ]
}
```

- **说明**：管理员无用户上下文，`priceAfterDiscount` 与 `basePrice` 相同（视为原价）

---

### 12. 查看房间占用情况

返回该房间自今日起所有被占用的日期字符串列表。

- **URL**：`GET /api/admin/getroomstatus`
- **Query 参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roomId | String | ✅ | 房间 id |

**请求示例**：

```
GET /api/admin/getroomstatus?roomId=1892345678901234101
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": [
    "2026-11-15",
    "2026-11-16",
    "2026-11-20",
    "2026-11-21",
    "2026-11-25",
    "2026-11-26"
  ]
}
```

- **说明**：元素为 `"YYYY-MM-DD"` 格式日期字符串

---

### 13. 查看所有订单（支持筛选）

所有筛选参数均可空；结果先按 status 升序、再按 orderTime 升序排列。

- **URL**：`GET /api/admin/allOrders`
- **Query 参数（通过 `@ModelAttribute` 绑定）**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roomName | String | ❌ | 按房间名称筛选（精确匹配） |
| time | LocalDateTime | ❌ | 按"当晚有人入住"筛选（占用时段与 [time, time+1天) 有重叠） |
| telephone | String | ❌ | 按用户电话筛选（精确匹配） |
| status | Integer | ❌ | 按订单状态筛选 |

**请求示例 1**：查询全部订单

```
GET /api/admin/allOrders
```

**请求示例 2**：筛选"状态为待确认、电话为 13812345678"的订单

```
GET /api/admin/allOrders?status=0&telephone=13812345678
```

**请求示例 3**：查询 2026-11-20 当晚有人入住的订单

```
GET /api/admin/allOrders?time=2026-11-20T00:00:00
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": [
    {
      "orderId": "1892345678901235100",
      "userId": "1892345678901234567",
      "userName": "小明同学",
      "userTelephone": "13800000001",
      "roomId": "1892345678901234101",
      "roomName": "301",
      "categoryName": "豪华大床房",
      "status": 0,
      "orderTime": "2026-11-06T10:30:00",
      "checkInTime": "2026-11-20T14:00:00",
      "checkOutTime": "2026-11-22T12:00:00",
      "cancelTime": null,
      "actualStartTime": null,
      "actualEndTime": null,
      "totalPrice": 1058.40,
      "viplevel": 2,
      "remark": "需要无烟房，带早餐"
    },
    {
      "orderId": "1892345678901235001",
      "userId": "1892345678901234567",
      "userName": "小明同学",
      "userTelephone": "13800000001",
      "roomId": "1892345678901234101",
      "roomName": "301",
      "categoryName": "豪华大床房",
      "status": 3,
      "orderTime": "2026-11-10T20:15:30",
      "checkInTime": "2026-11-15T14:00:00",
      "checkOutTime": "2026-11-17T12:00:00",
      "cancelTime": null,
      "actualStartTime": "2026-11-15T14:30:00",
      "actualEndTime": "2026-11-17T11:45:00",
      "totalPrice": 1058.40,
      "viplevel": 2,
      "remark": "需要无烟房"
    }
  ]
}
```

---

### 14. 查询普通用户列表

**至少传入 userName 或 telephone 其中一个**，否则返回错误。

- **URL**：`POST /api/admin/searchusers`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | String | ❌ | 精确匹配用户名（仅传此字段时生效） |
| telephone | String | ❌ | 精确匹配手机号（传了 userName 会被忽略） |

**请求示例 1**：按用户名查询

```json
{
  "userName": "小明同学"
}
```

**请求示例 2**：按手机号查询

```json
{
  "telephone": "13812345678"
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": [
    {
      "roleType": 1,
      "userTnfo": {
        "id": "1892345678901234567",
        "username": "小明同学",
        "viplevel": 2,
        "telephone": "13812345678",
        "exp": 320
      }
    }
  ]
}
```

---

### 15. 修改订单状态

用于确认订单、拒绝订单、入住、退房完成等操作。

- **URL**：`POST /api/admin/setorderstatus`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | String | ✅ | 订单 id |
| status | Integer | ✅ | 目标状态（见订单状态枚举） |

**请求示例 1**：确认订单（待确认 → 已确认未入住）

```json
{
  "orderId": "1892345678901235100",
  "status": 1
}
```

**请求示例 2**：拒绝订单（自动写入 cancelTime）

```json
{
  "orderId": "1892345678901235100",
  "status": 5
}
```

**请求示例 3**：办理入住（自动写入 actualStartTime）

```json
{
  "orderId": "1892345678901235100",
  "status": 2
}
```

**请求示例 4**：办理退房完成（自动写入 actualEndTime，并增加积分、更新 VIP）

```json
{
  "orderId": "1892345678901235100",
  "status": 3
}
```

**成功响应示例**：

```json
{
  "code": "200",
  "msg": "success",
  "data": null
}
```

- **副作用**：
  - status = 2（已入住）：自动写入 `actualStartTime = 当前时间`
  - status = 3（已退房完成）：自动写入 `actualEndTime = 当前时间`，并根据结算价给用户增加积分、更新 VIP 等级
  - status = 5（已拒绝）：自动写入 `cancelTime = 当前时间`

---

## 数据库表一览

| 表名 | 实体类 | 说明 |
|------|--------|------|
| user_account | User | 普通用户账户 |
| admin_account | Admin | 管理员账户 |
| category | Category | 房型 |
| room | Room | 物理房间 |
| orders | Order | 订单 |
| discount | Discount | VIP 等级 × 房型 折扣配置 |
| room_occupation | RoomOccupation | 房间占用时间段记录 |
| panorama_node | PanoramaNode | VR 全景节点 |
| panorama_link | PanoramaLink | VR 全景节点间跳转链接 |

### 金额相关字段（已统一为 DECIMAL）

| 表 | 字段 | Java 类型 | 说明 |
|----|------|-----------|------|
| category | basicPrice | BigDecimal | 房型每晚基础价 |
| orders | price | BigDecimal | 订单最终结算价 |
| discount | vipDiscount | BigDecimal | 折扣系数（如 0.900 = 9 折） |