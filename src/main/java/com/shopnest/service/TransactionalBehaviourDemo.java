package com.shopnest.service;

import com.shopnest.entity.*;
import com.shopnest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionalBehaviourDemo {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ── REQUIRED — default propagation ───────────────
    // Uses existing transaction or creates new one
    @Transactional
    public ProductEntity saveProduct(ProductEntity product) {
        log.debug("Saving product inside transaction: {}", product.getName());
        return productRepository.save(product);
    }

    // ── READ ONLY — for all queries ───────────────────
    // Hibernate skips dirty checking — performance boost
    @Transactional(readOnly = true)
    public ProductEntity findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    // ── ROLLBACK DEMO ─────────────────────────────────
    // RuntimeException triggers automatic rollback
    @Transactional
    public void demonstrateRollback(Long productId) {
        ProductEntity product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        // This change will be rolled back because exception is thrown
        product.setName("TEMP NAME — will rollback");
        productRepository.save(product);

        log.debug("About to throw — this save will be rolled back");

        // RuntimeException → @Transactional rolls back automatically
        throw new RuntimeException("Simulated failure — rolling back transaction");
    }

    // ── REQUIRES_NEW — independent transaction ────────
    // Always runs in its own transaction
    // Even if caller rolls back — this commits independently
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(String action, Long entityId) {
        log.debug("Audit log saved: {} for entity {}", action, entityId);
        // In real app — save to audit_logs table
        // This commits even if the parent transaction rolls back
    }

    // ── TRANSACTION BOUNDARY DEMO ─────────────────────
    // Shows how @Transactional works at method boundary
    @Transactional
    public void transactionBoundaryDemo(Long productId) {
        // Transaction START

        ProductEntity product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        // Product is PERSISTENT here — Hibernate tracking
        BigDecimal originalPrice = product.getPrice();

        // Dirty check — Hibernate will auto-generate UPDATE
        product.setPrice(originalPrice.add(BigDecimal.valueOf(100)));

        // No explicit save() needed — dirty checking handles it

        // Transaction END → flush → commit → UPDATE SQL generated
        log.debug("Transaction boundary — UPDATE will fire on commit");

        // Restore for demo purposes
        product.setPrice(originalPrice);
    }
}