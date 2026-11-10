# Kafka 消费闭环与接口限流

订单提交 → SQL 订单与 Outbox 同事务保存 → Kafka commerce.events → OrderEventConsumer → consumed_events 与系统通知同事务落库。消费者组为 shopverse-order-notifications-v1，关闭自动提交，按记录提交 offset。事务提交后再确认；重复事件按 eventId 主键去重。旧事件无 eventId 时使用 ORDER_CREATED:orderNo，兼容历史回放。系统通知可通过 /api/messages 查询，conversationId 为 system。

消费失败每秒重试且不主动丢弃消息；本阶段没有实现 DLQ，坏消息可能阻塞所在分区，需要监控消费者日志并修复。其他事件类型忽略。首次启用使用 earliest，会消费历史订单并产生历史通知。

Bucket4j 8.11.0 通过 Spring Filter 对 /api 和 /api/** 生效。单实例全部客户端共享容量 100 的桶，每秒整体补充 100 个令牌；超限返回 429、JSON RATE_LIMITED、Retry-After: 1。静态页面和 actuator 不消耗额度。此为周期令牌桶，不是任意滑动一秒严格最多 100 次：周期边界可以出现突发。多实例为每实例 100，未来集群总配额需要分布式桶或统一网关。不能替代用户鉴权或网络层 DDoS 防护。

已有 MySQL 需先执行：
```sql
CREATE TABLE IF NOT EXISTS consumed_events (
  event_id VARCHAR(200) PRIMARY KEY,
  consumed_at DATETIME(6)
);
```
新环境 schema-mysql.sql 已包含该表。

2026-11-10 Docker 验证：历史 600 条及新增 1 条订单事件产生 601 条消费记录、601 条系统通知。手动将最后一条事件再次投递 Kafka，两表数量仍为 601。并发请求 /api/home 250 次得到 121 次 200、129 次 429（请求跨补充周期，不能当成单个一秒窗口）。新增限流单元测试验证桶耗尽后拦截且静态资源不受影响。

压测脚本需单独统计 429；旧库存压测在限流启用后不能直接套用“600 成功 + 200 库存拒绝”的预期。吞吐、库存不足、限流拒绝分别统计。

测试使用 subclass Mockito mock maker，避免本机中文 Maven 路径下 Java agent 自附加失败；若后续测试需要 mock final/static 方法，再配置受支持的显式 agent。

Bucket4j API 参考：https://bucket4j.com/8.11.0/release-notes.html
