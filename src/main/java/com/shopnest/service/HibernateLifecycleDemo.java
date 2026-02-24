package com.shopnest.service;

import com.shopnest.entity.*;
import com.shopnest.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HibernateLifecycleDemo {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

//    @Bean
    @Profile("dev")
    public CommandLineRunner demonstrateLifecycle() {
        return args -> {
            System.out.println("\n==============================================");
            System.out.println("  EC-015B — Hibernate Lifecycle & @Transactional");
            System.out.println("==============================================");

            demonstrateTransientState();
            demonstratePersistentState();
            demonstrateDirtyChecking();
            demonstrateDetachedState();
            demonstrateTransactionalPropagation();
            demonstrateReadOnlyTransaction();

            System.out.println("\n==============================================");
            System.out.println("  EC-015B Demo Complete");
            System.out.println("==============================================\n");
        };
    }

    // ── 1. TRANSIENT STATE ────────────────────────────
    private void demonstrateTransientState() {
        System.out.println("\n--- 1. TRANSIENT STATE ---");

        // NEW — Hibernate knows nothing about this object
        ProductEntity product = ProductEntity.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(999))
                .stockQty(10)
                .active(true)
                .build();

        System.out.println("  State: TRANSIENT");
        System.out.println("  ID: " + product.getId());        // null — no ID yet
        System.out.println("  Hibernate tracking: NO");
        System.out.println("  SQL generated: NONE");
        System.out.println("  → Just a plain Java object in memory");
    }

    // ── 2. PERSISTENT STATE ───────────────────────────
    @Transactional
    private void demonstratePersistentState() {
        System.out.println("\n--- 2. PERSISTENT STATE ---");

        // Fetch existing product — now PERSISTENT
        ProductEntity product = productRepository.findById(1L).orElse(null);
        if (product == null) return;

        System.out.println("  State: PERSISTENT");
        System.out.println("  ID: " + product.getId());        // has ID
        System.out.println("  Hibernate tracking: YES");
        System.out.println("  Name: " + product.getName());
        System.out.println("  → Hibernate watching every field change");
    }

    // ── 3. DIRTY CHECKING ─────────────────────────────
    @Transactional
    public void demonstrateDirtyChecking() {
        System.out.println("\n--- 3. DIRTY CHECKING ---");

        ProductEntity product = productRepository.findById(1L).orElse(null);
        if (product == null) return;

        String originalName = product.getName();
        System.out.println("  Before: " + product.getName());

        // No explicit save() call needed!
        // Hibernate detects the change and generates UPDATE automatically
        // at the end of the transaction
        product.setName(product.getName() + " [verified]");
        System.out.println("  After setName() — no save() called");
        System.out.println("  Hibernate will auto-generate UPDATE SQL");
        System.out.println("  → This is DIRTY CHECKING");

        // Restore original name
        product.setName(originalName);
        System.out.println("  Restored: " + product.getName());
    }

    // ── 4. DETACHED STATE ─────────────────────────────
    public void demonstrateDetachedState() {
        System.out.println("\n--- 4. DETACHED STATE ---");

        // findById opens and closes its own transaction
        // After it returns → product is DETACHED
        ProductEntity product = productRepository.findById(1L).orElse(null);
        if (product == null) return;

        System.out.println("  State: DETACHED (transaction closed)");
        System.out.println("  ID: " + product.getId());        // still has ID
        System.out.println("  Changes tracked: NO");
        System.out.println("  product.setName() here → no SQL generated");
        System.out.println("  Must call repo.save(product) to reattach");
        System.out.println("  → save() on detached = UPDATE, on new = INSERT");

        // LazyInitializationException warning
        System.out.println("\n  ⚠️  LazyInitializationException:");
        System.out.println("  Accessing product.getReviews() here would throw");
        System.out.println("  because reviews is LAZY and session is closed");
        System.out.println("  Fix: use JOIN FETCH or @Transactional on caller");
    }

    // ── 5. @TRANSACTIONAL PROPAGATION ────────────────
    @Transactional
    public void demonstrateTransactionalPropagation() {
        System.out.println("\n--- 5. @TRANSACTIONAL PROPAGATION ---");

        System.out.println("  REQUIRED (default):");
        System.out.println("  → Uses existing transaction if one exists");
        System.out.println("  → Creates new one if none exists");
        System.out.println("  → Most common — used on all service methods");

        System.out.println("\n  REQUIRES_NEW:");
        System.out.println("  → Always creates a NEW transaction");
        System.out.println("  → Suspends existing transaction");
        System.out.println("  → Use for audit logs — must save even if main tx rolls back");

        System.out.println("\n  NESTED:");
        System.out.println("  → Savepoint inside existing transaction");
        System.out.println("  → Can rollback nested part without rolling back parent");

        System.out.println("\n  Isolation levels:");
        System.out.println("  READ_COMMITTED   → cannot read uncommitted changes (default MySQL)");
        System.out.println("  REPEATABLE_READ  → same query returns same result in transaction");
        System.out.println("  SERIALIZABLE     → full isolation, lowest concurrency");

        System.out.println("\n  Rollback rules:");
        System.out.println("  @Transactional rolls back on RuntimeException automatically");
        System.out.println("  @Transactional(rollbackFor=Exception.class) for checked exceptions");
        System.out.println("  @Transactional(noRollbackFor=BusinessException.class) to skip");
    }

    // ── 6. READ-ONLY TRANSACTION ──────────────────────
    @Transactional(readOnly = true)
    public void demonstrateReadOnlyTransaction() {
        System.out.println("\n--- 6. @Transactional(readOnly=true) ---");

        long count = productRepository.count();

        System.out.println("  Products in DB: " + count);
        System.out.println("  readOnly=true benefits:");
        System.out.println("  → Hibernate skips dirty checking — faster");
        System.out.println("  → Database can use read replicas");
        System.out.println("  → Flush mode set to NEVER — no accidental writes");
        System.out.println("  → Use on ALL query/read service methods");
    }
}