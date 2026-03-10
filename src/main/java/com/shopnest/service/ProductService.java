package com.shopnest.service;

import com.shopnest.dto.request.CreateProductRequest;
import com.shopnest.dto.request.UpdateProductRequest;
import com.shopnest.dto.response.ProductResponse;
import com.shopnest.entity.ProductEntity;
import com.shopnest.exception.InsufficientStockException;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.mapper.ProductMapper;
import com.shopnest.repository.CategoryRepository;
import com.shopnest.repository.ProductRepository;
import com.shopnest.repository.UserRepository;
import com.shopnest.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
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
    private final ProductMapper productMapper;

    // ── READ — cacheable ──────────────────────────────

    // Cache the full product list page
    // key includes page+size+sortBy so different pages
    // are cached independently
    @Cacheable(value = "products",
            key = "#page + '-' + #size + '-' + #sortBy")
    public PageResponse<ProductResponse> getAllProducts(
            int page, int size, String sortBy) {
        log.debug("Cache MISS — loading products from DB " +
                "page={} size={} sortBy={}", page, size, sortBy);
        return PageResponse.from(
                productRepository
                        .findAllWithCategory(PageRequest.of(page, size,
                                Sort.by(sortBy)))
                        .map(productMapper::toResponse));
    }

    // Cache individual product by id
    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.debug("Cache MISS — loading product {} from DB", id);
        return productMapper.toResponse(
                productRepository.findByIdWithCategory(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product", id)));
    }

    // Not cached — category filtering changes too often
    public PageResponse<ProductResponse> getProductsByCategory(
            Long categoryId, int page, int size) {
        return PageResponse.from(
                productRepository
                        .findByCategoryIdAndActiveTrue(categoryId,
                                PageRequest.of(page, size))
                        .map(productMapper::toResponse));
    }

    // Not cached — search results vary too much
    public PageResponse<ProductResponse> searchProducts(
            String keyword, int page, int size) {
        return PageResponse.from(
                productRepository
                        .searchByKeyword(keyword,
                                PageRequest.of(page, size))
                        .map(productMapper::toResponse));
    }

    public PageResponse<ProductResponse> getProductsByPriceRange(
            BigDecimal min, BigDecimal max, int page, int size) {
        return PageResponse.from(
                productRepository
                        .findByPriceRange(min, max,
                                PageRequest.of(page, size))
                        .map(productMapper::toResponse));
    }

    // ── WRITE — evict cache on mutation ──────────────

    // When a product is created, evict all product list
    // caches so next GET loads fresh data from DB
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(
            CreateProductRequest request) {
        log.debug("Cache EVICT — products (new product created)");

        var category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category", request.getCategoryId()));

        ProductEntity product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setActive(true);

        return productMapper.toResponse(
                productRepository.save(product));
    }

    // When a product is updated, evict both:
    // - the individual product cache (product::id)
    // - all product list caches (products::*)
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product",   key = "#id"),
            @CacheEvict(value = "products",  allEntries = true)
    })
    public ProductResponse updateProduct(
            Long id, UpdateProductRequest request) {
        log.debug("Cache EVICT — product:{} and products:*", id);

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", id));

        productMapper.updateEntity(product, request);
        return productMapper.toResponse(product);
    }

    // Evict on delete too
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product",   key = "#id"),
            @CacheEvict(value = "products",  allEntries = true)
    })
    public void deleteProduct(Long id) {
        log.debug("Cache EVICT — product:{} and products:*", id);

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", id));
        product.setActive(false);
    }

    // Evict on restock too
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product",   key = "#id"),
            @CacheEvict(value = "products",  allEntries = true)
    })
    public ProductResponse restockProduct(Long id, int quantity) {
        log.debug("Cache EVICT — product:{} and products:*", id);

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", id));
        product.addStock(quantity);
        return productMapper.toResponse(product);
    }
}