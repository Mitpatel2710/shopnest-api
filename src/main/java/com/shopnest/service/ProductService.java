package com.shopnest.service;

import com.shopnest.dto.request.CreateProductRequest;
import com.shopnest.dto.request.UpdateProductRequest;
import com.shopnest.dto.response.ProductResponse;
import com.shopnest.entity.CategoryEntity;
import com.shopnest.entity.ProductEntity;
import com.shopnest.entity.UserEntity;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.mapper.ProductMapper;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;          // ← added

    // ── GET ALL — paginated ───────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<ProductEntity> products = productRepository.findAll(pageable);
        return PageResponse.from(products.map(productMapper::toResponse));
    }

    // ── GET BY ID ─────────────────────────────────────
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(findProductById(id));
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
        return PageResponse.from(products.map(productMapper::toResponse));
    }

    // ── GET BY CATEGORY SLUG ──────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategorySlug(
            String slug, int page, int size) {
        CategoryEntity category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "slug", slug));
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductEntity> products = productRepository
                .findByCategoryIdAndActiveTrue(category.getId(), pageable);
        return PageResponse.from(products.map(productMapper::toResponse));
    }

    // ── SEARCH ────────────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(
            String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductEntity> products = productRepository
                .searchByKeyword(keyword, pageable);
        return PageResponse.from(products.map(productMapper::toResponse));
    }

    // ── PRICE RANGE ───────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByPriceRange(
            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<ProductEntity> products = productRepository
                .findByPriceRange(minPrice, maxPrice, pageable);
        return PageResponse.from(products.map(productMapper::toResponse));
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

        // MapStruct converts request → entity
        ProductEntity product = productMapper.toEntity(request);
        product.setActive(true);
        product.setCategory(category);
        product.setSeller(seller);

        ProductEntity saved = productRepository.save(product);
        log.debug("Created product: {} with id: {}", saved.getName(), saved.getId());
        return productMapper.toResponse(saved);
    }

    // ── UPDATE ────────────────────────────────────────
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        ProductEntity product = findProductById(id);

        // MapStruct updates only non-null fields
        productMapper.updateEntity(product, request);

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", request.getCategoryId()));
            product.setCategory(category);
        }

        // No explicit save() — dirty checking handles UPDATE
        log.debug("Updated product: {}", id);
        return productMapper.toResponse(product);
    }

    // ── DELETE (soft delete) ──────────────────────────
    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = findProductById(id);
        product.setActive(false);
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
        return productMapper.toResponse(product);
    }

    // ── PRIVATE HELPERS ───────────────────────────────
    private ProductEntity findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}