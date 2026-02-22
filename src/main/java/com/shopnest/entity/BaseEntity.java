package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity — shared fields for all JPA entities.
 *
 * HIBERNATE LIFECYCLE STATES:
 *
 * TRANSIENT   → new Entity() — not tracked, no ID
 *               SQL: none
 *
 * PERSISTENT  → after persist() or findById() inside @Transactional
 *               Hibernate tracks ALL field changes (dirty checking)
 *               SQL: UPDATE auto-generated at flush
 *
 * DETACHED    → after transaction closes
 *               Changes NOT tracked — must call save() to reattach
 *               Accessing LAZY collections → LazyInitializationException
 *               SQL: none until reattached
 *
 * REMOVED     → after remove() called
 *               SQL: DELETE on commit
 *
 * FIRST-LEVEL CACHE:
 *   EntityManager caches entities per transaction.
 *   findById(1L) called twice → only ONE SQL query.
 *   Second call returns cached instance.
 */

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}