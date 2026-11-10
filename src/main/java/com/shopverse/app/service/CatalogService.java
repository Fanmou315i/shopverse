package com.shopverse.app.service;

import com.shopverse.app.domain.Product;
import com.shopverse.app.repo.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CatalogService {
    private final ProductRepository products;
    public CatalogService(ProductRepository products) { this.products = products; }
    public List<Product> list(String query, String category) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        return products.findAll().stream()
            .filter(product -> category == null || category.isBlank() || category.equals(product.getCategory()))
            .filter(product -> normalized.isBlank() || (product.getName() + product.getDescription()).toLowerCase().contains(normalized))
            .toList();
    }
    public Product get(Long id) { return products.findById(id).orElseThrow(() -> new IllegalArgumentException("商品不存在")); }
}
