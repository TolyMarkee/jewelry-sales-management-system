# 云服务器部署指南

## 一、服务器环境准备

### 1.1 安装必要软件（CentOS/Ubuntu）

```bash
# === CentOS 7/8 ===
yum install -y java-17-openjdk java-17-openjdk-devel mysql-server nginx git

# === Ubuntu 20.04/22.04 ===
apt update
apt install -y openjdk-17-jdk mysql-server nginx git

# 验证安装
java -version     # 应显示 17.x
mysql --version   # 应显示 8.0.x
nginx -v          # 应显示 1.x
```

### 1.2 启动 MySQL 并创建数据库

```bash
# 启动 MySQL
systemctl start mysqld
systemctl enable mysqld

# 获取临时密码（CentOS）
grep 'temporary password' /var/log/mysqld.log

# 登录并修改密码
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourStrongPassword!';
FLUSH PRIVILEGES;

# 创建数据库
CREATE DATABASE jewelry_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE jewelry_db;

# 导入初始化脚本（从本地上传 init.sql 后执行）
source /opt/jewelry/sql/init.sql;
source /opt/jewelry/sql/migration_v2.sql;
EXIT;
```

### 1.3 配置防火墙

```bash
# 开放端口
firewall-cmd --add-port=8088/tcp --permanent   # 后端
firewall-cmd --add-port=80/tcp --permanent      # Nginx 前端
firewall-cmd --reload

# 或者用 iptables
iptables -I INPUT -p tcp --dport 8088 -j ACCEPT
iptables -I INPUT -p tcp --dport 80 -j ACCEPT
```

---

## 二、后端部署

### 2.1 打包 Spring Boot 项目

```bash
# 在本地项目目录执行
cd D:\springbootVue3\jewelry-server

# 修改 application.yml 中的数据库连接为云服务器地址
# spring.datasource.url=jdbc:mysql://localhost:3306/jewelry_db...
# spring.datasource.password=YourStrongPassword!

mvn clean package -DskipTests

# 生成的 jar 包位置: target/jewelry-server-1.0.0.jar
```

### 2.2 上传到云服务器

```bash
# 使用 scp 上传（在本地执行）
scp target/jewelry-server-1.0.0.jar root@你的服务器IP:/opt/jewelry/
scp src/main/resources/sql/init.sql root@你的服务器IP:/opt/jewelry/sql/
scp src/main/resources/sql/migration_v2.sql root@你的服务器IP:/opt/jewelry/sql/
```

### 2.3 启动后端服务

```bash
# SSH 到服务器
ssh root@你的服务器IP

# 创建启动脚本
cat > /opt/jewelry/start.sh << 'EOF'
#!/bin/bash
nohup java -jar /opt/jewelry/jewelry-server-1.0.0.jar \
  --server.port=8088 \
  > /opt/jewelry/app.log 2>&1 &
echo "Jewelry server started, PID: $!"
EOF

chmod +x /opt/jewelry/start.sh

# 启动
/opt/jewelry/start.sh

# 检查日志
tail -f /opt/jewelry/app.log
```

### 2.4 配置 Systemd 服务（推荐）

```bash
cat > /etc/systemd/system/jewelry.service << 'EOF'
[Unit]
Description=Jewelry Sales Management System
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/jewelry
ExecStart=/usr/bin/java -jar /opt/jewelry/jewelry-server-1.0.0.jar --server.port=8088
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable jewelry
systemctl start jewelry
systemctl status jewelry
```

---

## 三、前端部署

### 3.1 构建前端

```bash
# 在本地项目目录执行
cd D:\springbootVue3\jewelry-web

# 修改 vue.config.js 中的代理目标为云服务器地址
# 生产环境不需要 devServer proxy，直接构建即可

npm run build

# 生成的静态文件在 dist/ 目录
```

### 3.2 上传到服务器

```bash
# 上传 dist 目录
scp -r dist/* root@你的服务器IP:/opt/jewelry/web/
```

### 3.3 配置 Nginx

```bash
# 在服务器上配置 Nginx
cat > /etc/nginx/conf.d/jewelry.conf << 'EOF'
server {
    listen       80;
    server_name  你的域名或IP;

    # 前端静态文件
    location / {
        root   /opt/jewelry/web;
        index  index.html;
        try_files $uri $uri/ /index.html;  # Vue History 模式
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://localhost:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 图片代理
    location /images/ {
        proxy_pass http://localhost:8088;
    }
}
EOF

# 重启 Nginx
nginx -t && systemctl restart nginx
```

### 3.4 配置 SSL（推荐）

```bash
# 使用 Let's Encrypt 免费证书
yum install -y certbot python3-certbot-nginx
certbot --nginx -d 你的域名
```

---

## 四、验证部署

### 4.1 检查服务状态

```bash
systemctl status jewelry    # 后端 Java 服务
systemctl status nginx      # 前端 Nginx
systemctl status mysqld     # 数据库
```

### 4.2 API 测试

```bash
# 在服务器上测试
curl -X POST http://localhost:8088/api/login \
  -d "username=admin&password=123456"

# 从外部测试（替换为你的服务器 IP）
curl -X POST http://你的服务器IP/api/login \
  -d "username=admin&password=123456"
```

### 4.3 浏览器访问

```
http://你的服务器IP
或
https://你的域名
```

登录：admin / 123456

---

## 五、注意事项

| 注意点 | 说明 |
|--------|------|
| 数据库密码 | 生产环境务必使用强密码 |
| JWT 密钥 | 部署前修改 `application.yml` 中的 `jwt.secret` |
| 日志管理 | 定期清理 `/opt/jewelry/app.log` |
| 备份策略 | 每天备份 MySQL 数据库 |
| 文件上传 | 上传目录在 jar 包同级目录，需确保可写 |
| HTTPS | 生产环境建议开启 SSL |
| 内存配置 | 云服务器建议 2GB+ 内存 |

---

## 六、快速部署命令汇总

```bash
# 1. 安装软件
yum install -y java-17-openjdk mysql-server nginx git

# 2. 创建目录
mkdir -p /opt/jewelry/{sql,web}

# 3. 上传文件（本地执行）
scp jewelry-server-1.0.0.jar root@IP:/opt/jewelry/
scp -r dist/* root@IP:/opt/jewelry/web/
scp sql/*.sql root@IP:/opt/jewelry/sql/

# 4. 服务器上执行
mysql -u root -p < /opt/jewelry/sql/init.sql
mysql -u root -p < /opt/jewelry/sql/migration_v2.sql
systemctl start jewelry
systemctl restart nginx
```
