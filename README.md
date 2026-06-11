# modbus-vertx

基于 Vert.x、Mutiny、Hibernate Reactive 和 Modbus4J 的 Modbus TCP 数据采集服务。应用启动后会从 MySQL 加载设备、寄存器定位器和模板配置，周期性读取 Modbus 设备数据，并将采集结果发布到 MQTT；同时提供 HTTP API 和静态 Web 页面用于维护设备与模板配置。

## 功能特性

- Modbus TCP 设备连接管理、断线重连和周期轮询
- 设备、寄存器定位器、寄存器模板的 HTTP CRUD 接口
- 采集数据通过 MQTT 发布
- 基于 Hibernate Reactive 的 MySQL 数据访问
- 内置静态 Web 管理页面，资源位于 `src/main/resources/webroot`
- 支持配置文件和环境变量覆盖配置

## 技术栈

- Java 17
- Vert.x 4.5.x
- SmallRye Mutiny Vert.x
- Hibernate Reactive
- MySQL 8
- Modbus4J
- MQTT
- Maven

## 项目结构

```text
.
├── API.http                              # HTTP 接口调试示例
├── Dockerfile                           # 应用镜像构建文件
├── docker-compose.yml                   # MySQL + 应用编排示例
├── init-sql/modbus_vertx.sql            # 数据库初始化脚本
├── pom.xml                              # Maven 项目配置
├── src/main/java/io/github/kukpt/modbus # 应用源码
├── src/main/resources/config            # 默认配置
└── src/main/resources/webroot           # 静态 Web 页面
```

## 环境要求

- JDK 17 或更高版本
- Maven 3.8+，也可以直接使用项目内置的 `./mvnw`
- MySQL 8
- MQTT Broker
- 可访问的 Modbus TCP 设备或模拟器

## 快速启动

### 1. 初始化数据库

创建数据库后导入初始化脚本：

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p modbus_vertx < init-sql/modbus_vertx.sql
```

如果使用 `docker-compose.yml` 中的 MySQL 服务，`init-sql` 目录会挂载到 `/docker-entrypoint-initdb.d`，容器首次初始化时会自动执行脚本。

### 2. 修改配置

默认配置文件位于：

```text
src/main/resources/config/modbus-vertx.yaml
```

本地运行时也可以在项目根目录创建：

```text
config/modbus-vertx.yaml
```

应用会读取默认配置、本地配置和环境变量；环境变量优先级更高。常用配置如下：

| 配置项 | 说明 | 示例 |
| --- | --- | --- |
| `_MOD_DB_URL` | MySQL JDBC 地址 | `jdbc:mysql://127.0.0.1:12067/modbus_vertx` |
| `_MOD_DB_USER` | MySQL 用户名 | `modbus_vertx` |
| `_MOD_DB_PASSWORD` | MySQL 密码 | `******` |
| `_MOD_HTTP_PORT` | HTTP 服务端口 | `17888` |
| `_MOD_MODBUS_POOL_SIZE` | Modbus 轮询工作线程数 | `4` |
| `_MOD_MODBUS_QUERY_INTERVAL` | Modbus 轮询间隔，单位秒 | `2` |
| `_MOD_MODBUS_RECONNECTION_INTERVAL` | 断线重连间隔，单位秒 | `10` |
| `_MOD_MQTT_CLIENT_HOST` | MQTT Broker 地址 | `127.0.0.1` |
| `_MOD_MQTT_CLIENT_PORT` | MQTT Broker 端口 | `1883` |
| `_MOD_MQTT_CLIENT_USERNAME` | MQTT 用户名 | `user` |
| `_MOD_MQTT_CLIENT_PASSWORD` | MQTT 密码 | `password` |

### 3. 启动应用

开发运行：

```bash
./mvnw clean compile exec:java
```

打包：

```bash
./mvnw clean package
```

运行 fat jar：

```bash
java -jar target/modbus-vertx-1.0.0-SNAPSHOT-fat.jar
```

启动后访问：

```text
http://127.0.0.1:17888
```

## Docker

构建镜像：

```bash
docker build -t modbus-vertx:latest .
```

使用 Compose 启动前，需要准备 `.env` 文件。示例：

```env
MYSQL_ROOT_PASSWORD=root_password
MYSQL_DATABASE=modbus_vertx
MYSQL_USER=modbus_vertx
MYSQL_PASSWORD=modbus_password
DB_PORT=12067
DB_DATA_PATH=./data/mysql

MODBUS_WEB_SERVER_PORT=17888
MODBUS_QUERY_INTERVAL=2

MQTT_CLIENT_USERNAME=user
MQTT_CLIENT_PASSWORD=password
MQTT_CLIENT_HOST=127.0.0.1
MQTT_CLIENT_PORT=1883
MQTT_PUBLISH_TOPIC=/device/modbus/message/report
```

启动：

```bash
docker compose up -d
```

> 注意：当前 `docker-compose.yml` 中 `modbus-app` 使用的是临时镜像地址。如果需要使用本地构建镜像，请将 `image` 改为 `modbus-vertx:latest`，或按需补充 `build: .`。

## HTTP API

接口示例见 `API.http`。默认服务地址：

```text
http://127.0.0.1:17888
```

### 设备

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/device/` | 新增设备 |
| `PUT` | `/device/` | 修改设备 |
| `DELETE` | `/device/:id` | 删除设备并移除连接 |
| `GET` | `/device/:id` | 查看设备详情 |
| `GET` | `/device/list/:page/:pageSize` | 分页查询设备 |
| `POST` | `/device/apply/:id` | 应用设备配置并加入或替换 Modbus 连接 |

设备请求示例：

```json
{
  "name": "测试设备",
  "useIp": "192.168.1.222",
  "usePort": 502,
  "registerTemplateId": 4,
  "tagName": "test_device"
}
```

### 寄存器定位器

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/device/locator` | 新增定位器 |
| `PUT` | `/device/locator` | 修改定位器 |
| `DELETE` | `/device/locator/:id` | 删除定位器 |
| `GET` | `/device/locator/:id` | 查看定位器 |
| `GET` | `/device/locator/list/:page/:pageSize` | 分页查询定位器 |

定位器请求示例：

```json
{
  "type": "NUMERIC_LOCATOR",
  "name": "温度",
  "slaveId": 1,
  "dataType": 8,
  "registerBit": -1,
  "registerRange": 3,
  "registerOffset": 0,
  "tagName": "temperature"
}
```

`registerRange` 对应 Modbus4J 的寄存器范围：

| 值 | 含义 |
| --- | --- |
| `1` | Coil Status |
| `2` | Input Status |
| `3` | Holding Register |
| `4` | Input Register |

`dataType` 对应 Modbus4J 的 `DataType` 常量，具体数值请按实际设备协议配置。

### 寄存器模板

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/device/template` | 新增模板 |
| `PUT` | `/device/template` | 修改模板 |
| `DELETE` | `/device/template/:id` | 删除模板 |
| `GET` | `/device/template/:id` | 查看模板 |
| `GET` | `/device/template/list/:page/:pageSize` | 分页查询模板 |

模板请求示例：

```json
{
  "name": "泵站模板",
  "tagName": "pump",
  "version": 1,
  "locators": [1, 3, 16]
}
```

## MQTT 数据格式

采集结果会发布到如下主题：

```text
/device/{templateTagName}/{deviceTagName}/message/report
```

当模板或设备未配置 `tagName` 时，会回退使用对应 ID。

消息示例：

```json
{
  "deviceId": 19,
  "deviceName": "入库-泵站1",
  "locators": [
    {
      "locatorId": 1,
      "locatorName": "40001 入库泵温度1",
      "value": 23.5,
      "javaType": "java.lang.Float",
      "tagName": "temp",
      "ts": "2026-05-15T10:57:09"
    }
  ]
}
```

## 测试

运行单元测试：

```bash
./mvnw clean test
```

## 常见问题

### 数据库连接失败

检查 `_MOD_DB_URL`、`_MOD_DB_USER`、`_MOD_DB_PASSWORD` 是否正确，并确认 MySQL 已启动且数据库脚本已导入。

### 设备没有采集数据

检查设备 IP、端口、从站地址、寄存器范围、偏移量和数据类型是否与设备协议一致。修改设备或模板后，可以调用 `POST /device/apply/:id` 让配置立即生效。

### MQTT 没有收到消息

确认 `_MOD_MQTT_CLIENT_HOST`、`_MOD_MQTT_CLIENT_PORT`、用户名和密码正确；同时检查设备是否已连接成功并有采集值产生。
