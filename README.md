# 基于Spring Boot + Vue的珠宝首饰销售管理系统的设计与实现

> Design and Implementation of Jewelry Sales Management System Based on Spring Boot + Vue

基于 **Spring Boot 2.7 + Vue 3 + Element Plus + MyBatis-Plus + MySQL** 的全栈珠宝首饰销售管理系统。

## 项目结构

```
springbootVue3/
├── jewelry-server/          # Spring Boot 后端（端口 8080）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/jewelry/
│       │   ├── JewelryApplication.java       # 启动类
│       │   ├── common/        # R响应 / JWT工具 / 全局异常处理
│       │   ├── config/        # 跨域 / 拦截器 / 分页插件 / 静态资源
│       │   ├── controller/    # 7个控制器
│       │   ├── entity/        # 7个实体类
│       │   ├── mapper/        # 7个Mapper接口
│       │   └── service/       # 6个Service + 6个Impl
│       └── resources/
│           ├── application.yml
│           ├── static/images/ # 商品图片存放目录
│           └── sql/init.sql   # 数据库初始化脚本
│
└── jewelry-web/              # Vue 前端（端口 8081）
    ├── package.json
    ├── vue.config.js
    └── src/
        ├── api/index.js      # 后端API封装
        ├── utils/request.js  # Axios拦截器
        ├── router/index.js   # 路由 + 登录守卫
        ├── store/index.js    # Vuex状态管理
        └── views/            # 9个页面组件
```

---

## 一、环境准备

### 必需软件

| 软件 | 版本要求 | 下载 |
|------|---------|------|
| JDK | **1.8 或更高** | https://adoptium.net |
| MySQL | **8.0 或 5.7** | https://dev.mysql.com/downloads |
| Maven | **3.6+** | https://maven.apache.org |
| Node.js | **14+** | https://nodejs.org |

### 检查环境

```bash
java -version          # 确认 JDK 版本 >= 1.8
mysql --version        # 确认 MySQL 已安装
mvn -version           # 确认 Maven 版本
node -v && npm -v      # 确认 Node.js 版本 >= 14
```

---

## 二、数据库配置

### 1. 启动 MySQL 服务

确保 MySQL 服务正在运行。

### 2. 执行初始化脚本

用 Navicat / MySQL Workbench / 命令行执行：

```bash
mysql -u root -p < jewelry-server/src/main/resources/sql/init.sql
```

该脚本会：
- 创建 `jewelry_db` 数据库
- 创建 7 张业务表
- 插入示例数据（8件商品、3个客户、5个分类、2个用户）

### 3. 修改数据库连接信息

编辑 `jewelry-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jewelry_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root        # 改成你的MySQL用户名
    password: root        # 改成你的MySQL密码
```

### 4. 数据库表说明

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `user` | 用户表 | username, password(MD5), role(admin/sales) |
| `category` | 商品分类表 | name(戒指/项链/手镯/耳环/手链) |
| `product` | 商品表 | name, category_id, material, price, stock |
| `customer` | 客户表 | name, phone, email, address |
| `order` | 订单表 | order_no, customer_id, user_id, total_amount, status |
| `order_item` | 订单明细表 | order_id, product_id, quantity, price |
| `stock_record` | 库存记录表 | product_id, type(in/out), quantity |

**默认账号**：`admin` / `123456` （密码为 MD5 加密的 `e10adc3949ba59abbe56e057f20f883e`）

---

## 三、启动项目

### 启动后端（先启动）

```bash
cd jewelry-server
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 启动前端（后启动）

```bash
cd jewelry-web
npm install        # 首次运行需要安装依赖
npm run serve
```

前端运行在 `http://localhost:8081`

### 访问系统

浏览器打开 `http://localhost:8081`，使用 **admin / 123456** 登录。

---

## 四、测试图片说明

### 图片存放位置

商品图片放在 `jewelry-server/src/main/resources/static/images/` 目录下。

### 图片命名建议

```
images/
├── ring-01.jpg        # 戒指类
├── ring-02.jpg
├── necklace-01.jpg    # 项链类
├── necklace-02.jpg
├── bracelet-01.jpg    # 手镯类
├── earring-01.jpg     # 耳环类
└── chain-01.jpg       # 手链类
```

### 图片使用方式

1. 在商品管理的编辑弹窗中，`image` 字段填写图片文件名（如 `ring-01.jpg`）
2. 前端显示时拼接路径：`/images/` + 图片名 → `http://localhost:8080/images/ring-01.jpg`

### 获取测试图片

推荐以下方式获取免费测试图片：
- **Unsplash**：https://unsplash.com/s/photos/jewelry （免费高质量珠宝图片）
- **Pexels**：https://www.pexels.com/search/jewelry/
- **占位图服务**：`https://via.placeholder.com/400x400.png?text=Jewelry`

---

## 五、常见问题与解决方案

### 问题 1：启动后端报 `CommunicationsException: Communications link failure`

**原因**：MySQL 服务未启动，或连接信息配置错误。

**解决**：
- 确认 MySQL 服务已启动（Windows：`services.msc` 查看 MySQL 服务）
- 检查 `application.yml` 中的 `url`、`username`、`password` 是否正确
- 确认 `jewelry_db` 数据库已创建

### 问题 2：启动报 `Public Key Retrieval is not allowed`

**原因**：MySQL 8.0 的 SSL 连接限制。

**解决**：在 `application.yml` 的数据库连接 URL 末尾加上 `&allowPublicKeyRetrieval=true`：
```yaml
url: jdbc:mysql://localhost:3306/jewelry_db?...&allowPublicKeyRetrieval=true
```

### 问题 3：npm install 失败或很慢

**原因**：npm 默认源在国外，网络慢。

**解决**：设置淘宝镜像：
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### 问题 4：端口被占用

**原因**：8080 或 8081 端口已被其他程序使用。

**解决**：
- 后端：修改 `application.yml` 中 `server.port`
- 前端：修改 `vue.config.js` 中 `devServer.port`，同时修改 `proxy.target` 指向后端新端口

### 问题 5：登录报 401 或 Token 过期

**原因**：JWT Token 默认有效期 24 小时。

**解决**：
- 重新登录获取新 Token
- 或修改 `application.yml` 中 `jwt.expire` 值（单位：毫秒）

### 问题 6：前端请求报 CORS 跨域错误

**原因**：跨域配置未生效，或请求路径不对。

**解决**：
- 确认后端 `WebMvcConfig.java` 跨域配置正确
- 确认前端 `vue.config.js` 中 proxy 代理配置正确
- 前端开发环境所有 `/api` 请求通过代理转发到后端，不会有跨域问题
- 如果是生产部署，需要在后端配置 CORS 允许前端域名

### 问题 7：数据库表 `order` 是 MySQL 保留字

**原因**：`order` 是 SQL 关键字。

**解决**（已在代码中处理）：
- 后端实体类使用了 `@TableName("\`order\`")` 注解
- SQL 语句中使用了反引号包裹：`FROM \`order\``
- 如果手动写 SQL，记得用反引号包裹

### 问题 8：图片上传后无法显示

**原因**：图片存放路径或访问路径配置不对。

**解决**：
- 图片放在 `resources/static/images/` 目录
- 通过 `StaticResourceConfig.java` 映射了 `/images/**` 路径
- 访问 URL：`http://localhost:8080/images/xxx.jpg`
- 前端商品管理中 image 字段只需填文件名即可

### 问题 9：Maven 下载依赖很慢

**解决**：配置阿里云 Maven 镜像。编辑 Maven 的 `settings.xml`：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <name>aliyun</name>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

### 问题 10：ECharts 图表不显示

**原因**：可能是 `echarts` 导入方式问题。

**解决**：
- 确认 `package.json` 中已安装 `echarts`
- 确认 `SalesStatistics.vue` 中使用 `import * as echarts from 'echarts'`（而不是 `import echarts from 'echarts'`）
- 确认图表容器 `div` 有明确的宽高

---

## 六、API 接口文档

### 认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/login` | 登录，参数：`username`, `password` |

### 商品管理 `/api/product`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list?page=&limit=&keyword=&categoryId=` | 分页搜索 |
| GET | `/all` | 全部商品 |
| POST | `/save` | 新增 |
| PUT | `/update` | 修改 |
| DELETE | `/delete/{id}` | 删除 |

### 分类管理 `/api/category`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 全部列表 |
| POST | `/save` | 新增 |
| PUT | `/update` | 修改 |
| DELETE | `/delete/{id}` | 删除 |

### 订单管理 `/api/order`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list?page=&limit=&orderNo=&status=` | 分页搜索 |
| POST | `/save` | 创建订单（含明细） |
| PUT | `/updateStatus` | 更新状态 |
| DELETE | `/delete/{id}` | 删除 |

### 客户管理 `/api/customer`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list?page=&limit=&keyword=` | 分页搜索 |
| GET | `/all` | 全部客户 |
| POST | `/save` | 新增 |
| PUT | `/update` | 修改 |
| DELETE | `/delete/{id}` | 删除 |

### 库存管理 `/api/stock`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list?page=&limit=` | 记录列表 |
| POST | `/in` | 入库 |
| POST | `/out` | 出库 |

### 用户管理 `/api/user`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list?keyword=` | 列表 |
| POST | `/save` | 新增 |
| PUT | `/update` | 修改 |
| DELETE | `/delete/{id}` | 删除 |

---

## 七、前端UI升级日志 (v2.0)

### 技术栈升级
- **Vue 2 → Vue 3.4**：Composition API + `<script setup>` 语法
- **Vue CLI → Vite 5**：开发构建速度提升 2 倍以上
- **Element UI → Element Plus 2.7**：Vue 3 原生支持
- **Vuex → Pinia**：更简洁的状态管理
- **新增 Tailwind CSS 3.4**：原子化样式，开发效率提升
- 全局 Element Plus 暗色主题覆盖（分页、弹窗、输入框、表格等）

### 登录页（参考 21st.dev 设计）
- 全屏分栏布局：左侧表单 + 右侧珠宝图片
- **动态插图**：紫→粉→琥珀旋转渐变边框，呼吸光晕，环绕闪光粒子
- **AppInput 组件**：原生 DOM 光标跟踪光条（radial-gradient 实时跟随，60fps）
- 社交图标翻转动画（scale-y + 颜色反转）
- 按钮倾斜光扫动效（skew + translateX）
- 鼠标跟踪三色光斑（紫/蓝/粉 blur-3xl）

### 侧边栏 — DockMorph 风格
- 毛玻璃面板（backdrop-blur + 半透明背景）
- 紫→靛渐变活跃指示器 + hover 缩放
- **LUXE GEM MANAGEMENT** 品牌标识：钻石图标 + 渐变文字

### 仪表盘 — Data Card Display 风格
- 4 张渐变光晕统计卡片（蓝/琥珀/绿/粉）
- 暗色 ECharts 图表（柱状图 + 饼图）
- 库存预警彩色标签（绿/黄/红）

### 订单管理 — OrderHistory 时间线
- 垂直时间线 + 发光颜色编码节点
- **AnimatedStatusBadge**：脉冲呼吸点 + 彩色状态标签
- 玻璃卡片悬浮效果 + 搜索图标栏

### 商品管理 — 内联分析 + 动态搜索
- **ExpandableSearch**：图标↔搜索框平滑伸缩动画
- **SuggestiveInput**：打字机轮播占位提示词
- 库存进度条（绿/黄/红渐变）
- 分类筛选暗色原生下拉

### 其他页面
- 分类管理：文件夹树卡片 + 渐变图标
- 客户管理：头像首字母 + SuggestiveInput 搜索
- 库存管理：入库/出库彩色标签 + 预警横幅
- 销售统计：暗色 ECharts + Emoji 统计卡片
- 个人中心：渐变头像环 + 分区表单卡片
- 用户管理：角色彩色头像（红=管理员，蓝=销售员）

### 全局设计系统
- 背景色：`#101214`（深黑）
- 表面色：`rgba(22, 26, 29, 0.5)`（玻璃面板）
- 边框色：`rgba(255, 255, 255, 0.04-0.06)`（微秒边框）
- 强调色：`#a855f7 → #6366f1`（紫→靛渐变）
- 圆角系统：8px-16px-20px

---

## 八、注意事项

1. **密码加密**：密码使用 BCrypt 加密存储，旧 MD5 用户登录时自动升级
2. **JWT Token**：登录后获取，后续请求在 Header 中携带 `Authorization: Bearer <token>`
3. **登录拦截**：除 `/api/login` 外，所有 `/api/**` 接口都需要 Token
4. **订单状态流转**：pending(待付款) → paid(已付款) → shipped(已发货) → completed(已完成)
5. **库存扣减**：创建订单时自动扣减库存，入库/出库操作同步更新库存
6. **事务管理**：订单创建和库存扣减在同一事务中，保证数据一致性
