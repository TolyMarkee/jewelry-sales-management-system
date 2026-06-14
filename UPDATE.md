# 项目更新到云服务器指南

> 日常开发在本地 → 测试通过 → 推送到服务器

---

## 一、快速更新流程图

```
本地修改代码 → 测试通过 → 打包/构建 → 上传到服务器 → 重启服务
                              │
                    后端: mvn package (jar)
                    前端: npm run build (dist/)
                    数据库: SQL 脚本
```

---

## 二、更新后端（最常用）

### 2.1 本地打包

```bash
cd D:\springbootVue3\jewelry-server
mvn clean package -DskipTests
# 生成: target\jewelry-server-1.0.0.jar
```

### 2.2 上传到服务器

```bash
# Windows 用 WinSCP 拖拽，或命令：
scp target\jewelry-server-1.0.0.jar root@你的IP:/opt/jewelry/
```

### 2.3 服务器上重启

```bash
ssh root@你的IP

# 停止旧进程
kill $(ps aux | grep jewelry-server | grep -v grep | awk '{print $2}')

# 启动新的
nohup java -jar /opt/jewelry/jewelry-server-1.0.0.jar \
  --server.port=8088 \
  --spring.datasource.password=你的数据库密码 \
  > /opt/jewelry/app.log 2>&1 &

# 确认启动成功
tail -f /opt/jewelry/app.log
# 看到 "Started JewelryApplication" 即成功
```

**更新不影响数据库数据**，只替换 jar 包。

---

## 三、更新前端

### 3.1 本地构建

```bash
cd D:\springbootVue3\jewelry-web
npm run build
# 生成: dist/
```

### 3.2 上传覆盖

```bash
# 先清空服务器上的旧文件
ssh root@你的IP "rm -rf /opt/jewelry/web/*"

# 上传新文件
scp -r dist\* root@你的IP:/opt/jewelry/web/
```

**前端更新不需要重启任何服务**，Nginx 自动读取新文件。刷新浏览器即生效。

---

## 四、更新数据库（慎用）

### 4.1 仅加表/加字段

```bash
# 本地写好 SQL（如 migration_v3.sql）
scp migration_v3.sql root@你的IP:/opt/jewelry/sql/
ssh root@你的IP "mysql -u root -p jewelry_db < /opt/jewelry/sql/migration_v3.sql"
```

### 4.2 修改数据

```bash
# 先备份！
ssh root@你的IP "mysqldump -u root -p jewelry_db > /opt/jewelry/backup_$(date +%Y%m%d).sql"

# 再执行更新
ssh root@你的IP "mysql -u root -p jewelry_db -e 'UPDATE product SET price=6999 WHERE id=1'"
```

---

## 五、一键更新脚本（推荐）

在服务器上创建 `/opt/jewelry/update.sh`：

```bash
#!/bin/bash
# 用法: ./update.sh backend  或  ./update.sh frontend 或 ./update.sh all

BACKUP_DIR="/opt/jewelry/backups"
LOG_FILE="/opt/jewelry/app.log"

case $1 in
  backend)
    echo "=== 更新后端 ==="
    # 备份旧 jar
    cp /opt/jewelry/jewelry-server-1.0.0.jar $BACKUP_DIR/server-$(date +%Y%m%d%H%M).jar
    # 停止
    kill $(ps aux | grep jewelry-server | grep -v grep | awk '{print $2}')
    sleep 2
    # 启动
    nohup java -jar /opt/jewelry/jewelry-server-1.0.0.jar \
      --server.port=8088 \
      --spring.datasource.password=你的密码 \
      > $LOG_FILE 2>&1 &
    echo "后端已重启"
    ;;

  frontend)
    echo "=== 更新前端 ==="
    echo "请先用 scp 上传 dist/* 到 /opt/jewelry/web/"
    ;;

  all)
    echo "=== 备份数据库 ==="
    mysqldump -u root -p你的密码 jewelry_db | gzip > $BACKUP_DIR/db-$(date +%Y%m%d%H%M).sql.gz
    echo "数据库已备份到 $BACKUP_DIR"
    $0 backend
    echo "前端文件请手动 scp 上传"
    ;;

  *)
    echo "用法: ./update.sh {backend|frontend|all}"
    ;;
esac
```

然后本地一行命令更新：

```bash
# 本地打包+上传+重启 一条龙
cd D:\springbootVue3\jewelry-server && mvn clean package -DskipTests && scp target\jewelry-server-1.0.0.jar root@你的IP:/opt/jewelry/ && ssh root@你的IP "/opt/jewelry/update.sh backend"
```

---

## 六、更新频率建议

| 更新内容 | 方式 | 影响 |
|---------|------|------|
| 修 bug / 改页面文字 | 只更新前端 | 无停机 |
| 加新接口 / 改业务逻辑 | 更新后端 jar | 短暂中断（<10秒） |
| 加表 / 加字段 | 更新后端 jar + 执行 SQL | 短暂中断 |
| 改数据库已有数据 | 直接 MySQL 操作 | 无需重启 |

---

## 七、紧急回滚

```bash
ssh root@你的IP
# 列出备份
ls /opt/jewelry/backups/
# 回滚到指定版本
cp /opt/jewelry/backups/server-202606141200.jar /opt/jewelry/jewelry-server-1.0.0.jar
kill $(ps aux | grep jewelry-server | grep -v grep | awk '{print $2}')
nohup java -jar /opt/jewelry/jewelry-server-1.0.0.jar --server.port=8088 > /opt/jewelry/app.log 2>&1 &
```
