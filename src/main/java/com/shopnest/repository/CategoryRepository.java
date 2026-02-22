package com.shopnest.repository;

import com.shopnest.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findBySlug(String slug);

    Optional<CategoryEntity> findByName(String name);

    boolean existsBySlug(String slug);

    // Find all root categories (no parent)
    List<CategoryEntity> findByParentIsNull();

    // Find all children of a category
    List<CategoryEntity> findByParentId(Long parentId);

    // Custom JPQL — find active categories only
    @Query("SELECT c FROM CategoryEntity c WHERE c.active = true ORDER BY c.name")
    List<CategoryEntity> findAllActive();
}