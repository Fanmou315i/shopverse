# ShopVerse

基于 Java 25 / Spring Boot 的全栈电商学习项目。前端使用 HTML、CSS 和 JavaScript，后端提供商品、购物车、订单、消息和个人资料 API；支持 MySQL、Redis 库存协调以及 Kafka 订单通知。

## 功能与技术

- 首页搜索、分类、推荐，商品详情与规格、商家页面。
- 购物车选择和合并结算、直接购买 API、优惠与收货信息快照。
- 个人资料、地址、收藏、订单；独立客服会话及演示自动回复。
- Redis Lua 原子扣库存、多商品预留锁；SQL 订单与 Outbox 同事务写入。
- Kafka 生产、订单通知消费、数据库事件 ID 去重。
- Bucket4j 单实例共享限流：API 每秒补充 100 令牌，超限返回 429。

技术栈：Java 25、Spring Boot 3.5.0、Spring Data JPA、H2 / MySQL 8.4、Redis 7.4、Kafka 3.9.0、Bucket4j 8.11.0、Maven、JMeter 5.6.3。

## 本地运行

安装 JDK 25 与 Maven，IDEA 或 VS Code 打开本目录：

```sh
mvn test
mvn package
java -jar target/shopverse-1.0.0.jar
```

访问 http://localhost:8080 。默认 H2 内存库与本地库存协调器，重启后数据不保留，不启动 Kafka 消费。健康检查：`/actuator/health`。

## Docker 完整链路

先完成 Maven 打包，再执行：

```sh
docker compose -p shopverse-benchmark -f compose.benchmark.yml up -d --build
```

访问 http://localhost:18080 。该配置运行单应用及 MySQL、Redis、Kafka，使用独立持久化卷；可设置 `BENCHMARK_PORT` 修改端口。配置内的账号密码仅供本地演示，请勿直接用于公网部署。初始化 SQL 只对空数据库卷生效，已有数据库需要手动应用新增表结构。

## 测试状态

2026-11-10 实际验证：

| 场景 | 结果 |
|---|---|
| 未启用限流的 Docker 下单基线 | 800 请求、600 单落库、库存 0、超卖 0；成功 P95 467 ms |
| 启用限流后的隔离下单测试 | 800 请求：522 次 201、278 次 429；剩余库存 78，库存守恒 |
| 上述成功下单延迟 | 平均 44.21 ms，P95 121 ms，P99 164 ms |
| Kafka 重复事件验证 | 重复投递后消费记录与通知数未增加 |
| Maven 测试 | 2 项通过，覆盖启动和限流基本行为 |
| 功能接口检查 | 42 项状态码检查，41 项符合预期；不等于完整业务验收 |

测试均在同机运行，短时结果不能视为生产容量。PowerShell 持续负载遇到客户端端口耗尽，结果不可作为服务器容量结论。用户已停止后续测试。

## 已知问题

- Kafka 系统通知已落库，但 `system` 会话未加入会话白名单，消息页面缺少入口。
- 最近页面检查中商品详情未显示直接购买按钮，前端入口需修复；API 已验证。
- 当前是共享演示账户，无真实认证授权、支付或物流服务；不适合直接公开部署。
- Redis 和数据库不是跨系统原子事务，崩溃与事务提交失败窗口仍需完善。
- 消费失败无限重试，未实现死信队列；限流为单实例周期令牌桶，不是集群滑动窗口。

## 项目结构

```text
src/main/java/com/shopverse/app/
  api/       REST 接口与限流
  domain/    JPA 实体
  repo/      数据访问
  service/   下单、商品、库存等业务
  events/    Outbox、Kafka 发布和消费
src/main/resources/static/  前端
src/test/                   自动化测试
database/                   MySQL 初始化脚本
```

架构、接口与局限详见 [PROJECT-TECHNICAL-REPORT.md](PROJECT-TECHNICAL-REPORT.md)。压测与部署操作见 [BENCHMARK-DOCKER.md](BENCHMARK-DOCKER.md)，消费与限流说明见 [KAFKA-RATE-LIMIT.md](KAFKA-RATE-LIMIT.md)。其他阶段文档为历史记录，当前状态以本 README 与技术报告为准。`work/`、构建产物、IDE 配置和本地环境文件不上传公共仓库。
