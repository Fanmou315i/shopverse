# 商品、会话与订单体验更新

- 品牌名称和图标是首页链接。
- 商品颜色映射到本地款式示意图片；生成图不是销售实物承诺。尺码不会改变产品外观。
- 消息首页显示四个会话，进入后独立读取和保存消息，前台每五秒刷新；自动回复明确标为演示，不是 WebSocket 或真人客服。
- 新订单保存购物项 JSON 快照，展示商品图、规格、单价、数量，支持详情查看。旧演示订单没有明细，明确提示而不伪造。
- 物流页展示订单状态；未发货订单没有实时位置。用户可打开 OpenStreetMap 模拟杭州中转站地图。真实物流需服务商、运单号和授权接口，目前未接通。
- 默认 H2 仍会随服务重启重置。MySQL profile 尚需完整联调与结构迁移，新字段包括 orders.items_json、chat_messages.conversation_id。

## 图片生成记录

使用内置 imagegen。耳机编辑提示：保持原照片的耳机形状、角度、黄色背景和灯光，只将耳机改为雾银灰。
其他款式生成提示：product-mockup；单件商品；对应颜色；浅灰背景；居中、全物体、自然阴影、真实摄影、无品牌、非卡通。资产为 images/2-white.png、2-gray.png、3-white.png、3-blue.png、4-black.png、4-green.png、5-fresh.png、5-moist.png、6-gray.png、6-white.png；耳机为 images/headphones-silver.png。

验证：Java 打包、JS 语法；会话隔离及自动回复；下单明细快照；浏览器颜色切换、图文订单、订单详情与模拟地图入口。地图加载依赖外网。
