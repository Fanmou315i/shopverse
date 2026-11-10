package com.shopverse.app.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    private String category;
    private String description;
    private String emoji;
    private Integer rating;
    private Integer soldCount;

    protected Product() { }

    public Product(String name, BigDecimal price, String category, String description, String emoji, Integer rating, Integer soldCount) {
        this.name = name; this.price = price; this.category = category; this.description = description;
        this.emoji = emoji; this.rating = rating; this.soldCount = soldCount;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getEmoji() { return emoji; }
    public Integer getRating() { return rating; }
    public Integer getSoldCount() { return soldCount; }
}
