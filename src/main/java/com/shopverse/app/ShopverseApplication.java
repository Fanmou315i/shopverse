package com.shopverse.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShopverseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopverseApplication.class, args);
    }
}
