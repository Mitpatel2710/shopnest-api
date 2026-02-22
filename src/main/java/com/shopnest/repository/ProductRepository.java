package com.shopnest.repository;

import com.shopnest.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    // Derived queries
    List<ProductEntity> findByCategoryId(Long categoryId);

    List<ProductEntity> findByActiveTrueAndStockQtyGreaterThan(int minStock);

    Page<ProductEntity> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    // Custom JPQL — search by keyword
    @Query("SELECT p FROM ProductEntity p " +
            "WHERE p.active = true " +
            "AND (p.name LIKE %:keyword% " +
            "OR p.description LIKE %:keyword%)")
    Page<ProductEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Custom JPQL — price range filter
    @Query("SELECT p FROM ProductEntity p " +
            "WHERE p.active = true " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<ProductEntity> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // Custom JPQL — products by seller
    @Query("SELECT p FROM ProductEntity p WHERE p.seller.id = :sellerId AND p.active = true")
    List<ProductEntity> findBySellerId(@Param("sellerId") Long sellerId);

    // @Modifying — update/delete queries need this
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stockQty = p.stockQty - :qty WHERE p.id = :id")
    int reduceStock(@Param("id") Long id, @Param("qty") int qty);

    // Aggregate — count by category
    @Query("SELECT p.category.name, COUNT(p) FROM ProductEntity p GROUP BY p.category.name")
    List<Object[]> countByCategory();
}