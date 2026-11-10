package com.shopverse.app.api;

import com.shopverse.app.service.CatalogService;
import com.shopverse.app.service.InventoryCoordinator;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ProductDetailController {
    private final CatalogService catalog;
    private final InventoryCoordinator inventory;
    public ProductDetailController(CatalogService catalog, InventoryCoordinator inventory) { this.catalog = catalog; this.inventory = inventory; }
    private static final String[][] DATA = {
        {"声域数码", "通勤与工作之间，留一点安静给自己", "包耳式设计搭配柔软耳垫，适合日常通勤、办公与影音使用。折叠结构便于收纳，附带收纳袋及连接线。", "曜石黑,雾银灰", "标准套装,旅行套装", "连接方式：蓝牙 / 3.5mm|充电接口：USB-C|包装清单：耳机、充电线、音频线、收纳袋", "photo-1505740420928-5e560c06d30e", "photo-1546435770-a3e426bf472b"},
        {"原野运动", "轻装出发，把日常变成运动场", "网眼鞋面配合缓震中底，兼顾透气与日常穿着舒适度。橡胶外底提供抓地力，适用于城市慢跑和日常步行。", "云白,岩灰", "39,40,41,42,43,44", "鞋面：织物|鞋底：橡胶 / EVA|适用场景：慢跑、通勤|护理：软刷清洁，自然晾干", "photo-1542291026-7eec264c27ff", "photo-1549298916-b41d501d3772"},
        {"日常生活研究所", "把一杯热饮，带进每一天", "双层杯体与旋拧杯盖，适合办公和日常携带。宽口设计方便清洗，请勿装入碳酸饮料或放入微波炉。", "雾白,深海蓝", "350mL,500mL", "杯体：不锈钢|杯盖：食品接触用 PP|清洁方式：手洗|包装清单：杯体、杯盖、说明书", "photo-1602143407151-7111542de6e8", "photo-1523362628745-0c100150b504"},
        {"原野运动", "从工作日，到下一段旅程", "主仓适合收纳日常衣物与随身物品，独立电脑隔层减少刮擦。前置拉链袋和侧袋方便取用小物，适合通勤及短途旅行。", "炭黑,橄榄绿", "20L,25L", "面料：聚酯纤维|闭合方式：拉链|电脑隔层：最大16英寸|护理：局部清洁，勿机洗", "photo-1553062407-98eeb64c6a62", "photo-1622260614153-03223fb72052"},
        {"日常生活研究所", "让日常清洁，回归简单", "泡沫型洁面产品，适合日常清洁流程。取适量加水起泡后轻柔按摩，再用清水洗净。首次使用建议局部试用。", "清润型,保湿型", "100mL,150mL", "产品类别：洁面|使用方法：湿面后起泡清洗|储存：阴凉避光|注意：避免接触眼睛", "photo-1608571423902-eed4a5ad8108", "photo-1556229010-6c3f2c9ca5f8"},
        {"声域数码", "给专注，留一束刚好的光", "适合阅读、办公与桌面照明。可调节灯臂方便控制照射方向，多档亮度适应不同环境。请使用随附电源配件。", "深灰,暖白", "桌夹款,底座款", "光源：LED|供电：USB-C|适用空间：书桌、工作台|包装：灯体、电源线、说明书", "photo-1507473885765-e6ed057f782c", "photo-1534073828943-f801091bb18c"},
        {"声域数码", "影像、性能与日常体验的平衡", "采用高刷新率屏幕和多摄像头系统，适合移动办公、影像记录与日常娱乐。", "深空灰,冰川蓝", "8GB+256GB,12GB+512GB,16GB+1TB", "处理器：旗舰移动平台|屏幕：6.7 英寸高刷屏|接口：USB-C|网络：5G 双卡双待", "phone", "phone"},
        {"声域数码", "把高效工作带到每一次出发", "轻薄金属机身配合高色域屏幕，面向文档处理、轻量创作和多任务办公。", "星空灰,月光银", "16GB+512GB,32GB+1TB,32GB+2TB", "屏幕：14 英寸高色域|处理器：高性能移动处理器|接口：USB-C / HDMI|系统：预装办公系统", "laptop", "laptop"},
        {"匠作手工坊", "把日常饮水变成可握在手里的作品", "采用手工拉坯与釉彩烧制，每只杯子的釉色流动和纹理都有细微差异。", "青釉,暖白釉", "单只装,礼盒装", "材质：高温陶瓷|容量：约 350mL|工艺：手工拉坯 / 上釉|说明：纹理存在自然差异", "ceramic", "ceramic"},
        {"匠作手工坊", "点燃一段安静而有香气的时间", "大豆蜡搭配木芯，燃烧时有轻微噼啪声；适合卧室、书桌和送礼场景。", "雪松,无花果", "180g,300g", "蜡材：大豆蜡|香型：植物调|燃烧时长：约 30-55 小时|包装：手工贴标", "candle", "candle"},
        {"匠作手工坊", "随时间留下属于自己的光泽", "植鞣皮革手工裁切、打磨和缝制，紧凑尺寸可收纳常用卡片与少量折叠钞票。", "原色,深棕", "四卡位,六卡位", "材质：头层植鞣革|工艺：手缝蜡线|尺寸：约 10×7cm|护理：避免长时间浸水", "leather", "leather"},
        {"声域数码", "安静输入，让桌面更专注", "紧凑布局释放桌面空间，支持热插拔和多设备连接，适合编程、写作与办公。", "石墨黑,雾白", "红轴-有线,茶轴-三模,静音轴-三模", "配列：75%|连接：有线 / 蓝牙 / 2.4G|键帽：PBT|轴体：热插拔", "keyboard", "keyboard"}
    };
    public Map<String,Object> detail(long id) {
        var p = catalog.get(id);
        var d = DATA[(int)((id-1)%DATA.length)];
        return Map.of("product", p, "merchant", d[0], "headline", d[1], "description", d[2], "colors", d[3].split(","), "styles", d[4].split(","), "specifications", d[5].split("\\|"), "images", images(id), "stock", inventory.available(id), "photoNotice", "实物场景参考摄影；商品、规格和店铺均为项目演示数据。");
    }
    private List<String> images(long id) {
        if (id <= 6) return List.of("/images/"+id+"-0.jpg", "/images/"+id+"-1.jpg");
        return switch ((int) id) {
            case 7 -> List.of("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=60", "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=900&q=60");
            case 8 -> List.of("https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=60", "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=900&q=60");
            case 9 -> List.of("https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=900&q=60", "https://images.unsplash.com/photo-1456221327007-ec1dff7a3b36?auto=format&fit=crop&w=900&q=60");
            case 10 -> List.of("https://images.unsplash.com/photo-1603006905003-be475563bc59?auto=format&fit=crop&w=900&q=60", "https://images.unsplash.com/photo-1608181831718-c9ffd2a1ab56?auto=format&fit=crop&w=900&q=60");
            case 11 -> List.of("https://images.unsplash.com/photo-1523779917675-b6ed3a42a561?auto=format&fit=crop&w=900&q=60", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=60");
            default -> List.of("https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=900&q=60", "https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=900&q=60");
        };
    }
    @GetMapping("/products/{id}") public Map<String,Object> product(@PathVariable long id) { return detail(id); }
    @GetMapping("/catalog-details") public List<Map<String,Object>> details() { return catalog.list("", "").stream().map(p -> detail(p.getId())).toList(); }
    @GetMapping("/merchants/{name}") public Map<String,Object> merchant(@PathVariable String name) {
        var items = details().stream().filter(p -> p.get("merchant").equals(name)).toList();
        if(items.isEmpty()) throw new IllegalArgumentException("商家不存在");
        return Map.of("name", name, "description", "认真挑选每一件日常用品。关注设计、材质与使用体验。", "products", items, "categories", items.stream().map(p -> ((com.shopverse.app.domain.Product)p.get("product")).getCategory()).distinct().toList());
    }
}
