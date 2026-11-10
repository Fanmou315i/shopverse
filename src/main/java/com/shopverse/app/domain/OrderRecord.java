package com.shopverse.app.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class OrderRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNo;
    @Column(precision = 10, scale = 2) private BigDecimal total;
    private int itemCount;
    private String status;
    private LocalDateTime createdAt;
    @Lob @Column(columnDefinition = "LONGTEXT") private String itemsJson;
    private String purchaseChannel;
    private String couponCode;
    private String paymentMethod;
    private String receiverName;
    private String receiverPhone;
    @Column(length = 300) private String receiverAddress;
    public String getItemsJson() { return itemsJson; }
    public void setItemsJson(String value) { itemsJson = value; }

    protected OrderRecord() { }
    public OrderRecord(String orderNo, BigDecimal total, int itemCount, String status) {
        this(orderNo, total, itemCount, status, "CART", "", "", "", "", "");
    }
    public OrderRecord(String orderNo, BigDecimal total, int itemCount, String status, String purchaseChannel, String couponCode, String paymentMethod, String receiverName, String receiverPhone, String receiverAddress) {
        this.orderNo = orderNo; this.total = total; this.itemCount = itemCount; this.status = status; this.createdAt = LocalDateTime.now();
        this.purchaseChannel = purchaseChannel; this.couponCode = couponCode; this.paymentMethod = paymentMethod;
        this.receiverName = receiverName; this.receiverPhone = receiverPhone; this.receiverAddress = receiverAddress;
    }
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public BigDecimal getTotal() { return total; }
    public int getItemCount() { return itemCount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getPurchaseChannel() { return purchaseChannel; }
    public String getCouponCode() { return couponCode; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
}
