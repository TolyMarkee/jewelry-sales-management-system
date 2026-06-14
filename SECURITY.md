# 安全防护方案

## 已实施的安全措施

### 一、代码层防护

| 措施 | 文件 | 说明 |
|------|------|------|
| **BCrypt 密码加密** | `SecurityBeans.java` + `UserServiceImpl.java` | 替换 MD5，Salt 随机，暴力破解难度极大 |
| **密码自动升级** | `UserServiceImpl.java:39-46` | 旧 MD5 用户登录时自动升级为 BCrypt |
| **XSS 过滤** | `XssFilter.java` | 所有请求参数 HTML 转义，防跨站脚本注入 |
| **SQL 注入防护** | MyBatis-Plus `?` 参数化 | 不拼接用户输入到 SQL（MyBatis-Plus 预编译） |
| **接口限流** | `RateLimitInterceptor.java` | 每 IP 每分钟最多 60 次请求，防暴力破解 |
| **密码响应隐藏** | `User.java` `@JsonProperty(WRITE_ONLY)` | 密码字段永远不会出现在 JSON 响应中 |
| **登录拦截** | `LoginInterceptor.java` | 所有 `/api/**`（除 login）需要 JWT Token |
| **密码强度校验** | `UserController.java:90` | 新密码至少 6 位 |
| **文件上传校验** | `FileUploadController.java:36-46` | 限制仅图片类型 + 最大 5MB |

### 二、服务器层防护（需在云服务器上配置）

#### 2.1 SSH 加固

```bash
# 1. 修改 SSH 默认端口（防扫描）
sed -i 's/#Port 22/Port 22022/' /etc/ssh/sshd_config
systemctl restart sshd

# 2. 禁止 root 直接登录，使用普通用户 + sudo
useradd -m -s /bin/bash deploy
passwd deploy
echo "deploy ALL=(ALL) ALL" >> /etc/sudoers
sed -i 's/PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config

# 3. 禁用密码登录，仅用 SSH 密钥
ssh-keygen -t ed25519 -C "deploy@jewelry"
ssh-copy-id deploy@你的服务器IP
sed -i 's/PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
systemctl restart sshd
```

#### 2.2 防火墙

```bash
# 使用 iptables 最小化开放端口
systemctl start firewalld

# 只开放必要端口
firewall-cmd --add-port=22022/tcp --permanent   # SSH（修改后的端口）
firewall-cmd --add-port=80/tcp --permanent      # HTTP
firewall-cmd --add-port=443/tcp --permanent     # HTTPS
# 8088 不对外开放（通过 Nginx 代理）
# 3306 不对外开放（仅本地访问）

firewall-cmd --reload
```

#### 2.3 Fail2Ban（防暴力破解）

```bash
yum install -y fail2ban

cat > /etc/fail2ban/jail.local << 'EOF'
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 5

[sshd]
enabled = true
port = 22022

[nginx-http-auth]
enabled = true

[nginx-botsearch]
enabled = true
EOF

systemctl enable fail2ban
systemctl start fail2ban
```

#### 2.4 MySQL 安全

```bash
# 1. 删除匿名用户和测试库
mysql -u root -p -e "
DELETE FROM mysql.user WHERE User='';
DROP DATABASE IF EXISTS test;
DELETE FROM mysql.db WHERE Db='test';
FLUSH PRIVILEGES;
"

# 2. 创建专用低权限账号（不给 root）
mysql -u root -p -e "
CREATE USER 'jewelry_app'@'localhost' IDENTIFIED BY '高强度的随机密码';
GRANT SELECT, INSERT, UPDATE, DELETE ON jewelry_db.* TO 'jewelry_app'@'localhost';
FLUSH PRIVILEGES;
"

# 3. 更新 application.yml 使用低权限账号
# spring.datasource.username=jewelry_app
# spring.datasource.password=高强度的随机密码
```

#### 2.5 Nginx 安全头

```bash
cat > /etc/nginx/conf.d/jewelry.conf << 'EOF'
server {
    listen 80;
    server_name _;

    # 安全头
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Permissions-Policy "camera=(), microphone=(), geolocation=()" always;

    # 隐藏 Nginx 版本号
    server_tokens off;

    # 限制请求体大小（防大文件上传攻击）
    client_max_body_size 5m;

    # 前端
    location / {
        root /opt/jewelry/web;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API（限流：每秒 10 个请求，突发 20）
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    location /api/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://127.0.0.1:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /images/ {
        proxy_pass http://127.0.0.1:8088;
    }
}
EOF

nginx -t && systemctl restart nginx
```

#### 2.6 自动更新

```bash
# 设置定时安全更新
yum install -y yum-cron
systemctl enable yum-cron
systemctl start yum-cron
```

### 三、应用配置安全

部署前修改 `application.yml`：

```yaml
# JWT 密钥改为随机字符串（不要用默认值！）
jwt:
  secret: $(openssl rand -base64 64 生成的随机字符串)
  expire: 86400000

# 生产环境关闭 SQL 日志
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl

# 数据库连接用低权限账号
spring:
  datasource:
    username: jewelry_app        # 不是 root！
    password: 高强度的随机密码
```

---

## 安全等级评估

| 攻击类型 | 防护状态 | 说明 |
|----------|---------|------|
| 暴力破解密码 | 已防护 | BCrypt + 限流 |
| SQL 注入 | 已防护 | MyBatis-Plus 参数化查询 |
| XSS 跨站脚本 | 已防护 | XssFilter 全局转义 |
| CSRF | 低风险 | JWT 无状态，非 Cookie 认证 |
| DDoS | 部分防护 | 限流拦截器 + Nginx limit_req |
| 中间人攻击 | 待配置 | 部署 SSL 证书（Let's Encrypt） |
| SSH 暴力破解 | 待配置 | Fail2Ban + 密钥登录 |
| 文件上传漏洞 | 已防护 | 类型校验 + 大小限制 |

---

## 快速安全检查清单

部署到云服务器后，逐项确认：

- [ ] SSH 端口已修改（非 22）
- [ ] 禁用 root SSH 登录
- [ ] 配置 SSH 密钥登录
- [ ] JWT secret 已改为随机值
- [ ] MySQL 使用独立低权限账号
- [ ] 防火墙仅开放 80/443/SSH端口
- [ ] Nginx 安全头已配置
- [ ] Fail2Ban 已启动
- [ ] 开启 SSL/HTTPS
- [ ] 数据库已备份
