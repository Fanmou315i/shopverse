package com.shopverse.app.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String productName;
    private String emoji;
    private String variant;
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    @Column(precision = 10, scale = 2) private BigDecimal unitPrice;
    private int quantity;
    private boolean selected;

    protected CartItem() { }
    public CartItem(Product product) {
        productId = product.getId(); productName = product.getName(); emoji = product.getEmoji();
        unitPrice = product.getPrice(); quantity = 1; selected = true;
    }
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getEmoji() { return emoji; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public boolean isSelected() { return selected; }
    public void changeQuantity(int quantity) { this.quantity = Math.max(1, quantity); }
    public void setSelected(boolean selected) { this.selected = selected; }
    public void increment() { quantity++; }
    public BigDecimal lineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
