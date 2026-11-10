package com.shopverse.app.service;

import com.shopverse.app.domain.CartItem;
import com.shopverse.app.domain.OrderRecord;
import com.shopverse.app.domain.Product;
import com.shopverse.app.events.CommerceEventGateway;
import com.shopverse.app.repo.CartItemRepository;
import com.shopverse.app.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartItemRepository cartItems;
    private final OrderRepository orders;
    private final CatalogService catalog;
    private final CommerceEventGateway events;
    private final InventoryCoordinator inventory;
    public CartService(CartItemRepository cartItems, OrderRepository orders, CatalogService catalog, CommerceEventGateway events, InventoryCoordinator inventory) {
        this.cartItems = cartItems; this.orders = orders; this.catalog = catalog; this.events = events; this.inventory = inventory;
    }
    public List<CartItem> list() { return cartItems.findAll(); }
    @Transactional
    public CartItem add(Long productId, String variant) {
        if (variant == null || variant.isBlank()) variant = "默认款";
        CartItem item = cartItems.findByProductIdAndVariant(productId, variant).orElseGet(() -> {
            Product product = catalog.get(productId);
            return new CartItem(product);
        });
        item.setVariant(variant);
        if (item.getId() != null) item.increment();
        CartItem saved = cartItems.save(item);
        events.publish("CART_UPDATED", "{\"productId\":" + productId + "}");
        return saved;
    }
    public CartItem update(Long id, int quantity, boolean selected) {
        CartItem item = cartItems.findById(id).orElseThrow(() -> new IllegalArgumentException("购物车商品不存在"));
        item.changeQuantity(quantity); item.setSelected(selected);
        return cartItems.save(item);
    }
    public void remove(Long id) { cartItems.deleteById(id); }
    @Transactional
    public void selectAll(boolean selected) { cartItems.findAll().forEach(item -> item.setSelected(selected)); cartItems.flush(); }
    @Transactional
    public OrderRecord checkout(PurchaseMeta meta) {
        List<CartItem> selected = cartItems.findAll().stream().filter(CartItem::isSelected).toList();
        if (selected.isEmpty()) throw new IllegalStateException("请先选择要结算的商品");
        BigDecimal total = selected.stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        total = applyCoupon(total, meta.couponCode());
        int count = selected.stream().mapToInt(CartItem::getQuantity).sum();
        Map<Long, Integer> quantities = selected.stream().collect(Collectors.groupingBy(CartItem::getProductId, LinkedHashMap::new, Collectors.summingInt(CartItem::getQuantity)));
        inventory.reserve(quantities);
        try {
            OrderRecord order = createOrder(total, count, selected.stream().map(item -> Map.<String, Object>of(
                "productId", item.getProductId(), "productName", item.getProductName(), "variant", item.getVariant(),
                "unitPrice", item.getUnitPrice(), "quantity", item.getQuantity())).toList(), "CART", meta);
            cartItems.deleteAll(selected);
            return order;
        } catch (RuntimeException e) { inventory.release(quantities); throw e; }
    }

    @Transactional
    public OrderRecord checkoutDirect(Long productId, String variant, int quantity, PurchaseMeta meta) {
        if (quantity < 1 || quantity > 99) throw new IllegalArgumentException("购买数量需在 1 到 99 之间");
        Product product = catalog.get(productId);
        BigDecimal total = applyCoupon(product.getPrice().multiply(BigDecimal.valueOf(quantity)), meta.couponCode());
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("productId", product.getId()); snapshot.put("productName", product.getName());
        snapshot.put("variant", variant == null || variant.isBlank() ? "默认款" : variant);
        snapshot.put("unitPrice", product.getPrice()); snapshot.put("quantity", quantity);
        Map<Long, Integer> quantities = Map.of(productId, quantity);
        inventory.reserve(quantities);
        try { return createOrder(total, quantity, List.of(snapshot), "DIRECT", meta); }
        catch (RuntimeException e) { inventory.release(quantities); throw e; }
    }

    private BigDecimal applyCoupon(BigDecimal total, String couponCode) {
        return "SAVE40".equals(couponCode) && total.compareTo(BigDecimal.valueOf(299)) >= 0
            ? total.subtract(BigDecimal.valueOf(40)) : total;
    }

    private OrderRecord createOrder(BigDecimal total, int count, List<?> items, String channel, PurchaseMeta meta) {
        String orderNo = "SV" + java.util.UUID.randomUUID().toString().replace("-", "");
        OrderRecord order = new OrderRecord(orderNo, total, count, "待发货", channel, meta.couponCode(), meta.paymentMethod(), meta.receiverName(), meta.receiverPhone(), meta.receiverAddress());
        try { order.setItemsJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(items)); }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException("订单明细保存失败", e); }
        order = orders.save(order);
        events.publish("ORDER_CREATED", "{\"orderNo\":\"" + orderNo + "\",\"total\":" + total + "}");
        return order;
    }
    public record PurchaseMeta(String couponCode, String paymentMethod, String receiverName, String receiverPhone, String receiverAddress) {
        public PurchaseMeta {
            couponCode = couponCode == null ? "" : couponCode;
            paymentMethod = paymentMethod == null ? "" : paymentMethod;
            receiverName = receiverName == null ? "" : receiverName;
            receiverPhone = receiverPhone == null ? "" : receiverPhone;
            receiverAddress = receiverAddress == null ? "" : receiverAddress;
        }
    }
}
