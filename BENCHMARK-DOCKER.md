# 真实中间件基线：第一阶段

更新：2026-11-10 已执行首轮重测，见 [实际报告](work/docker-baseline-report-20261110.md)。以下未安装 Docker 状态为早期记录，已解除。新增 account_data 建表定义及 LONGTEXT 映射修复已经通过真实 MySQL 启动检查。

状态：已准备隔离部署与参数化测试计划；尚未完成 Docker 实测。本机未发现 docker 命令、Docker 服务或默认安装目录的 Docker Desktop。现有 158.9 req/s、P95 9 ms 仍仅属于历史 H2 基线。

## 启动与执行

需要可运行 Linux 容器的 Docker Engine / Docker Desktop，以及 Java 25 / Maven。使用独立测试项目及数据卷，不复用演示数据库。端口 18080 为测试应用，原演示应用的 8080 不受影响。

```powershell
mvn -DskipTests package
docker compose -p shopverse-benchmark -f compose.benchmark.yml up -d --build
docker compose -p shopverse-benchmark -f compose.benchmark.yml ps
Invoke-RestMethod http://localhost:18080/actuator/health
# 首次查询会初始化库存；必须记录返回的库存，不能假设每次运行都是 600。
Invoke-RestMethod http://localhost:18080/api/products/8
```

在独立压测机器复制 JMX，替换 SERVER_IP 后运行：

```powershell
jmeter -n -t jmeter-order-stock.jmx -Jhost=SERVER_IP -Jport=18080 -Jthreads=80 -Jramp=5 -Jloops=10 -l baseline.jtl -j baseline.log -e -o baseline-html
```

每次使用新的结果文件和 HTML 目录。不要在库存已经耗尽的情况下直接重复测试并称为有效下单压力。库存边界测试之后的持续负载若全是库存拒绝，无法代表建单吞吐量。再次测试应使用新的隔离 Compose 项目名与空白数据卷，先停止旧测试应用释放 18080；不要删除演示数据。

## 证据采集与验收

- 测试前后读取商品库存，记录有效订单数量差、购买数量差；本 JMX 每单 1 件，成功订单数才等于购买件数。
- 使用 MySQL 查询核验订单而不是只数 HTTP 201：`SELECT COUNT(*), SUM(item_count) FROM orders WHERE payment_method='jmeter-stress';`。测试前后各查询一次。
- 核验 Outbox：`SELECT status, COUNT(*) FROM outbox_events GROUP BY status;`。停止负载后观察积压是否消退；Kafka 投递成功不等于下游消费完成。
- 保存 `docker compose -p shopverse-benchmark -f compose.benchmark.yml logs --no-color` 输出及 `docker stats --no-stream` 输出。
- 从 `/actuator/metrics` 列表中选择实际存在的 `hikaricp.connections.active`、`hikaricp.connections.pending`、`hikaricp.connections.timeout`、`http.server.requests` 指标采样。
- Redis 使用 `docker compose -p shopverse-benchmark -f compose.benchmark.yml exec -T redis redis-cli INFO stats`，按前后差计算 keyspace_hits / (keyspace_hits + keyspace_misses)。该指标是 Redis 全局键命中率，并非单独商品缓存命中率。
- Kafka 发送耗时应从实际暴露的 producer 指标或另加计时器取得；当前没有经过验证的采样结果，不填估算值。
- 统计全部请求与成功请求的平均值/P95/P99、吞吐量、状态码分布。400 必须结合响应体和服务端日志判断，锁繁忙等 400 不能一律归类为库存不足。
- 超卖数量 = max(本轮已持久化购买件数 - 初始可售库存, 0)。另核验结束库存与订单数量守恒、库存不为负。

## 建议采纳范围

P0：先单应用 + MySQL/Redis/Kafka，再做多应用实例；独立压测机缺失时如实标记同机资源竞争。P1：先做业务及中间件指标。后续再考虑订单消费者、事件去重/DLQ、JWT/RBAC 和 order_items 拆表。暂不将 Debezium、真实支付、全套 ELK 或 Redisson 引入本阶段。

锁职责澄清：现有 Redis 锁只覆盖 reserve 内的多商品库存预留，按商品 ID 排序降低循环等待风险；Lua 负责单商品原子检查和扣减。锁在 reserve 返回前释放，并未覆盖后续数据库事务，也不提供请求幂等或防重复提交。进程崩溃、Redis 丢失数据、租约过期后的多商品补偿，仍需后续故障测试及持久化库存账本设计。
