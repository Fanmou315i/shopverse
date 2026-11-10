package com.shopverse.app.config;

import com.shopverse.app.domain.ChatMessage;
import com.shopverse.app.domain.OrderRecord;
import com.shopverse.app.domain.Product;
import com.shopverse.app.repo.ChatMessageRepository;
import com.shopverse.app.repo.OrderRepository;
import com.shopverse.app.repo.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DemoDataConfig {
    @Bean
    CommandLineRunner loadDemoData(ProductRepository products, ChatMessageRepository messages, OrderRepository orders) {
        return args -> {
            if (products.count() == 0) {
                products.saveAll(List.of(
                    new Product("降噪耳机 Pro", new BigDecimal("499.00"), "数码", "40dB 主动降噪，通勤专注好伙伴", "🎧", 5, 3200),
                    new Product("轻盈跑鞋", new BigDecimal("359.00"), "运动", "缓震回弹，城市慢跑首选", "👟", 5, 1600),
                    new Product("智能保温杯", new BigDecimal("129.00"), "家居", "温度实时可见，316 不锈钢内胆", "🥤", 4, 980),
                    new Product("旅行双肩包", new BigDecimal("219.00"), "生活", "16 英寸电脑隔层与干湿分离", "🎒", 5, 770),
                    new Product("氨基酸洁面", new BigDecimal("89.00"), "美妆", "温和净澈，敏感肌可用", "🧴", 4, 2100),
                    new Product("桌面补光灯", new BigDecimal("159.00"), "数码", "无频闪三档色温，视频会议更精神", "💡", 5, 660),
                    new Product("Nova 智能手机 Pro", new BigDecimal("4299.00"), "数码", "旗舰影像与高刷屏幕，适合日常创作和通勤", "📱", 5, 1880),
                    new Product("Air 轻薄笔记本", new BigDecimal("6799.00"), "数码", "高效移动办公，轻薄机身与长续航兼顾", "💻", 5, 1260),
                    new Product("手工釉彩陶瓷杯", new BigDecimal("168.00"), "手工", "匠人手工拉坯，每只纹理略有不同", "🏺", 5, 420),
                    new Product("手作木芯香薰蜡烛", new BigDecimal("128.00"), "手工", "大豆蜡与木芯，营造安静的居家氛围", "🕯️", 4, 730),
                    new Product("植鞣皮革卡包", new BigDecimal("219.00"), "手工", "手工缝制，随使用留下自然光泽", "👝", 5, 350),
                    new Product("静音机械键盘", new BigDecimal("499.00"), "数码", "热插拔轴体与紧凑配列，适合长时间输入", "⌨️", 5, 950)
                ));
            }
            if (messages.count() == 0) {
                messages.save(new ChatMessage("客服小安", "你好，ShopVerse 客服在线。订单、售后和商品问题都可以告诉我。"));
                messages.save(new ChatMessage("系统通知", "双十一预售已开始，收藏商品享优先提醒。"));
            }
            if (orders.count() == 0) {
                orders.save(new OrderRecord("SV20261102001", new BigDecimal("219.00"), 1, "已完成"));
            }
        };
    }
}
