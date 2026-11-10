# Redis 库存防超卖模块

生产模式设置 `SHOPVERSE_REDIS_ENABLED=true` 并连接 Redis 后，库存使用 `shopverse:inventory:stock:{productId}` 缓存。每次提交订单会按商品 ID 排序获取 `shopverse:inventory:lock:{productId}` 分布式锁；锁持有者使用 Lua 脚本检查并原子扣减库存，库存不足时不会创建订单。

多商品订单若其中一项扣减失败，会补偿此前已扣减的 Redis 库存。订单持久化失败同样会释放已预扣库存。锁采用随机令牌和 10 秒过期时间，释放时用 Lua 校验持有者，避免错误释放其他实例的锁。

本地开发默认 `SHOPVERSE_REDIS_ENABLED=false`，会使用进程内并发锁回退实现，方便直接启动项目；它只适用于单实例开发环境。运行 Redis 可使用 `docker compose -f docker-compose.redis.yml up -d`，然后设置环境变量 `SHOPVERSE_REDIS_ENABLED=true`。
