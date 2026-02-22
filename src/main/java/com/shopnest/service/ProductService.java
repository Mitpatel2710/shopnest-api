package com.shopnest.service;

import com.shopnest.dto.request.CreateProductRequest;
import com.shopnest.dto.request.UpdateProductRequest;
import com.shopnest.dto.response.ProductResponse;
import com.shopnest.entity.CategoryEntity;
import com.shopnest.entity.ProductEntity;
import com.shopnest.entity.UserEntity;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.CategoryRepository;
import com.shopnest.repository.ProductRepository;
import com.shopnest.repository.UserRepository;
import com.shopnest.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ── GET ALL — paginated ───────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<ProductEntity> products = productRepository.findAll(pageable);
        return PageResponse.from(products.map(this::toResponse));
    }

    // ── GET BY ID ─────────────────────────────────────
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        ProductEntity product = findProductById(id);
        return toResponse(product);
    }

    // ── GET BY CATEGORY ───────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategory(
            Long categoryId, int page, int size) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductEntity> products = productRepository
                .findByCategoryIdAndActiveTrue(categoryId, pageable);
        return PageResponse.from(products.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategorySlug(
            String slug, int page, int size) {
        CategoryEntity category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "slug", slug));
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductEntity> products = productRepository
                .findByCategoryIdAndActiveTrue(category.getId(), pageable);
        return PageResponse.from(products.map(this::toResponse));
    }


    // ── SEARCH ────────────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(
            String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductEntity> products = productRepository
                .searchByKeyword(keyword, pageable);
        return PageResponse.from(products.map(this::toResponse));
    }

    // ── PRICE RANGE ───────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByPriceRange(
            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<ProductEntity> products = productRepository
                .findByPriceRange(minPrice, maxPrice, pageable);
        return PageResponse.from(products.map(this::toResponse));
    }

    // ── CREATE ────────────────────────────────────────
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", request.getCategoryId()));

        UserEntity seller = null;
        if (request.getSellerId() != null) {
            seller = userRepository.findById(request.getSellerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User", request.getSellerId()));
        }

        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQty(request.getStockQty())
                .imageUrl(request.getImageUrl())
                .type(request.getType())
                .brand(request.getBrand())
                .active(true)
                .category(category)
                .seller(seller)
                .build();

        ProductEntity saved = productRepository.save(product);
        log.debug("Created product: {} with id: {}", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    // ── UPDATE ────────────────────────────────────────
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        ProductEntity product = findProductById(id);

        if (request.getName()        != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice()       != null) product.setPrice(request.getPrice());
        if (request.getStockQty()    != null) product.setStockQty(request.getStockQty());
        if (request.getImageUrl()    != null) product.setImageUrl(request.getImageUrl());
        if (request.getType()        != null) product.setType(request.getType());
        if (request.getBrand()       != null) product.setBrand(request.getBrand());
        if (request.getActive()      != null) product.setActive(request.getActive());

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", request.getCategoryId()));
            product.setCategory(category);
        }

        // No explicit save() — dirty checking handles UPDATE
        log.debug("Updated product: {}", id);
        return toResponse(product);
    }

    // ── DELETE (soft delete) ──────────────────────────
    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = findProductById(id);
        product.setActive(false);   // soft delete — never hard delete products
        log.debug("Soft deleted product: {}", id);
    }

    // ── RESTOCK ───────────────────────────────────────
    @Transactional
    public ProductResponse restockProduct(Long id, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Restock quantity must be positive");
        ProductEntity product = findProductById(id);
        product.addStock(quantity);
        log.debug("Restocked product: {} by {}", id, quantity);
        return toResponse(product);
    }

    // ── PRIVATE HELPERS ───────────────────────────────
    private ProductEntity findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductResponse toResponse(ProductEntity p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQty(p.getStockQty())
                .imageUrl(p.getImageUrl())
                .active(p.isActive())
                .type(p.getType())
                .brand(p.getBrand())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .sellerId(p.getSeller() != null ? p.getSeller().getId() : null)
                .sellerName(p.getSeller() != null ? p.getSeller().getFullName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}