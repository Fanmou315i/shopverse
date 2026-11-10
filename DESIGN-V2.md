# 商城第二版

本版采用深灰、深蓝和白色，移除商品 Emoji，增加本地参考摄影、商品详情页、商家主页和规格入车。

## 页面与接口

- `/#product/1`：商品详情，包括两张参考图片、介绍、颜色与款式、参数及购买须知。
- `/#store/声域数码`：商家主页，包括推荐、新品排序和店内分类。
- `GET /api/products/{id}`：商品详情。
- `GET /api/catalog-details`：商品详情集合。
- `GET /api/merchants/{name}`：商家商品集合及分类。
- `POST /api/cart/items`：增加 `variant` 字段，同商品不同规格独立入车。

## 演示边界

目前是六件商品、三家店铺的演示目录。详情配置仍在 Java 中；新品按商品 ID 倒序，推荐按演示销量排序，尚未建设商家发布后台。图片是 Unsplash 类别参考摄影，不保证为同款产品的不同角度，已在页面中标注。未实现真实付款、库存 SKU 或商家经营。简历描述应如实限定为电商原型及所完成的接口与交互。

图片来源记录见工作区 `work/download-images.ps1` 中的 Unsplash photo ID；本地资源位于 `src/main/resources/static/images`。

验证：JDK 25 Maven 打包通过，JavaScript 语法检查通过；浏览器验证首页进入详情、选择雾银灰/旅行套装入车、商家页与新品排序，购物车显示所选规格。
