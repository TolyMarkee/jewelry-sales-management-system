# API 代码追溯对照表

> 验收要求：对于每个 URL `http://ip:port/请求路径`，能找到对应的前端代码和后端代码。

---

## 1. 登录模块

### `POST /api/login`

| 层级 | 文件路径 | 行号 | 说明 |
|------|---------|------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 5 | `request.post('/login', null, { params })` |
| 前端页面 | `jewelry-web/src/views/Login.vue` | 52-62 | 登录表单提交 |
| 前端状态 | `jewelry-web/src/store/index.js` | 15-18 | `setToken` / `setUser` 存入 Vuex |
| 后端控制器 | `jewelry-server/.../controller/LoginController.java` | 18-30 | `@PostMapping("/api/login")` |
| 后端服务 | `jewelry-server/.../service/impl/UserServiceImpl.java` | 24-42 | MD5加密 + 数据库查询 + JWT生成 |
| 后端数据 | `jewelry-server/.../mapper/UserMapper.java` | - | `BaseMapper<User>` 自动生成 SQL |
| 数据库表 | `jewelry-server/.../sql/init.sql` | 11-25 | `user` 表 |

---

## 2. 用户管理模块

### `GET /api/user/list`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 9 |
| 前端页面 | `jewelry-web/src/views/UserManage.vue` | 71-76 |
| 后端控制器 | `jewelry-server/.../controller/UserController.java` | 25-33 |
| 后端服务 | MyBatis-Plus 自动实现 | - |
| 数据库表 | `user` 表 | - |

### `GET /api/user/profile`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 21 |
| 前端页面 | `jewelry-web/src/views/UserProfile.vue` | 84-88 |
| 后端控制器 | `jewelry-server/.../controller/UserController.java` | 38-42 |
| 后端服务 | `userService.getById(userId)` | - |

### `PUT /api/user/profile`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 23 |
| 前端页面 | `jewelry-web/src/views/UserProfile.vue` | 90-98 |
| 后端控制器 | `jewelry-server/.../controller/UserController.java` | 47-57 |
| 数据库表 | `user` 表 | - |

### `PUT /api/user/password`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 26 |
| 前端页面 | `jewelry-web/src/views/UserProfile.vue` | 100-113 |
| 后端控制器 | `jewelry-server/.../controller/UserController.java` | 62-78 |

### `POST /api/user/save`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 12 |
| 前端页面 | `jewelry-web/src/views/UserManage.vue` | 91-96 |
| 后端控制器 | `jewelry-server/.../controller/UserController.java` | 83-87 |

### `PUT /api/user/update`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 后端控制器 | `jewelry-server/.../controller/UserController.java` | 92-100 |

### `DELETE /api/user/delete/{id}`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 后端控制器 | `jewelry-server/.../controller/UserController.java` | 105-109 |

---

## 3. 商品管理模块

### `GET /api/product/list?page=&limit=&keyword=&categoryId=`

| 层级 | 文件路径 | 行号 | 说明 |
|------|---------|------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 42 |
| 前端页面 | `jewelry-web/src/views/ProductManage.vue` | 118-124 | `loadData()` |
| 后端控制器 | `jewelry-server/.../controller/ProductController.java` | 23-27 |
| 后端服务 | `jewelry-server/.../service/impl/ProductServiceImpl.java` | 17-36 | 分页+搜索+联表查询 |
| 后端Mapper | `jewelry-server/.../mapper/ProductMapper.java` | 12-15 | LEFT JOIN category |
| 数据库表 | `product` JOIN `category` | - |

### `GET /api/product/all`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 45 |
| 前端页面 | `jewelry-web/src/views/OrderManage.vue` | 236 | 订单选择商品时加载 |
| 后端控制器 | `jewelry-server/.../controller/ProductController.java` | 32-36 |

### `POST /api/product/save` / `PUT /api/product/update` / `DELETE /api/product/delete/{id}`

| 后端控制器 | `jewelry-server/.../controller/ProductController.java` | 41-59 |

---

## 4. 订单管理模块

### `GET /api/order/list?page=&limit=&orderNo=&status=`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 76 |
| 前端页面 | `jewelry-web/src/views/OrderManage.vue` | 227-232 |
| 后端控制器 | `jewelry-server/.../controller/OrderController.java` | 24-28 |
| 后端服务 | `jewelry-server/.../service/impl/OrderServiceImpl.java` | 33-73 | 联表查询+明细填充 |
| 后端Mapper | `jewelry-server/.../mapper/OrderMapper.java` | 13-17 | LEFT JOIN customer, user |
| 数据库表 | `order` JOIN `customer` JOIN `user` | - |

### `POST /api/order/save`

| 层级 | 文件路径 | 行号 | 说明 |
|------|---------|------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 79 |
| 前端页面 | `jewelry-web/src/views/OrderManage.vue` | 242-252 | 构建订单数据 |
| 后端控制器 | `jewelry-server/.../controller/OrderController.java` | 33-39 |
| 后端服务 | `jewelry-server/.../service/impl/OrderServiceImpl.java` | 83-103 | **事务**：插入订单+明细+扣库存 |
| 数据库表 | `order` + `order_item` + `product`（库存扣减） | - |

### `PUT /api/order/updateStatus`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 后端控制器 | `jewelry-server/.../controller/OrderController.java` | 44-49 |

---

## 5. 客户管理模块

### `GET /api/customer/list?page=&limit=&keyword=`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 63 |
| 前端页面 | `jewelry-web/src/views/CustomerManage.vue` | 78-82 |
| 后端控制器 | `jewelry-server/.../controller/CustomerController.java` | 24-38 |
| 后端服务 | MyBatis-Plus 自动分页 | - |
| 数据库表 | `customer` | - |

### `GET /api/customer/all`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端页面 | `jewelry-web/src/views/OrderManage.vue` | 235 | 订单选择客户时加载 |
| 后端控制器 | `jewelry-server/.../controller/CustomerController.java` | 43-47 |

---

## 6. 库存管理模块

### `GET /api/stock/list`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 87 |
| 前端页面 | `jewelry-web/src/views/StockManage.vue` | 74-78 |
| 后端控制器 | `jewelry-server/.../controller/StockRecordController.java` | 23-27 |
| 后端服务 | `jewelry-server/.../service/impl/StockRecordServiceImpl.java` | 17-31 |
| 数据库表 | `stock_record` JOIN `product` | - |

### `POST /api/stock/in` （入库）

| 层级 | 文件路径 | 行号 | 说明 |
|------|---------|------|------|
| 前端页面 | `jewelry-web/src/views/StockManage.vue` | 94-97 |
| 后端控制器 | `jewelry-server/.../controller/StockRecordController.java` | 32-41 | **事务**：插入记录+更新商品库存 |
| 数据库表 | `stock_record` INSERT + `product` UPDATE | - |

### `POST /api/stock/out` （出库）

| 层级 | 文件路径 | 行号 | 说明 |
|------|---------|------|------|
| 后端控制器 | `jewelry-server/.../controller/StockRecordController.java` | 46-61 | 库存不足校验 |

---

## 7. 分类管理模块

### `GET /api/category/list`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 31 |
| 前端页面 | `jewelry-web/src/views/CategoryManage.vue` | 58-62 |
| 前端页面 | `jewelry-web/src/views/ProductManage.vue` | 114-117 | 商品表单加载分类下拉 |
| 后端控制器 | `jewelry-server/.../controller/CategoryController.java` | 18-21 |
| 数据库表 | `category` | - |

---

## 8. 文件上传模块

### `POST /api/upload/avatar`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 29 对应 uploadAvatar |
| 前端页面 | `jewelry-web/src/views/UserProfile.vue` | 11-20 | el-upload 组件 |
| 后端控制器 | `jewelry-server/.../controller/FileUploadController.java` | 22-28 |

### `POST /api/upload/product`

| 层级 | 文件路径 | 行号 |
|------|---------|------|
| 前端调用 | `jewelry-web/src/api/index.js` | 33 对应 uploadProductImage |
| 前端页面 | `jewelry-web/src/views/ProductManage.vue` | 73-82 | el-upload 组件 |
| 后端控制器 | `jewelry-server/.../controller/FileUploadController.java` | 33-36 |

---

## 9. AI 对话模块

### `POST /api/ai/chat`

| 层级 | 文件路径 | 行号 | 说明 |
|------|---------|------|------|
| 前端调用 | `jewelry-web/src/components/AiAssistant.vue` | 81 | `request.post('/ai/chat', ...)` |
| 后端控制器 | `jewelry-server/.../controller/AiController.java` | 26-91 | LLM API 代理 + 本地回退 |
| 配置文件 | `jewelry-server/.../application.yml` | 30-35 | `ai.api.url/key/model` |

---

## 10. 请求链路完整示例

以「创建订单」为例，追踪一次完整的前端→后端→数据库请求：

```
1. 浏览器输入: http://localhost:8081/#/order
   → Vue Router (router/index.js:35) 加载 OrderManage.vue

2. 用户点击"创建订单"，填写客户和商品，点击提交
   → OrderManage.vue:242 调用 saveOrder()
   → api/index.js:79 发送 POST /api/order/save
   → utils/request.js:12 Axios 拦截器自动添加 Authorization header

3. 请求到达后端
   → WebMvcConfig.java:30 LoginInterceptor 校验 Token
   → OrderController.java:33 @PostMapping("/api/order/save")
   → OrderServiceImpl.java:83 @Transactional createOrder()
     → 生成订单编号 "JWL20260614..."
     → OrderMapper.insert(order) → INSERT INTO `order` ...
     → OrderItemMapper.insert(item) → INSERT INTO order_item ...
     → ProductMapper.updateById(product) → UPDATE product SET stock=...
   → 返回 R.ok("下单成功")

4. 前端收到响应
   → utils/request.js:23 响应拦截器检查 code
   → OrderManage.vue:249 $message.success('下单成功')
   → loadData() 刷新订单列表

5. 数据库验证
   → SELECT * FROM `order` WHERE order_no = 'JWL20260614...'  -- 新订单
   → SELECT * FROM order_item WHERE order_id = ...              -- 新明细
   → SELECT stock FROM product WHERE id = ...                   -- 库存减少
```
