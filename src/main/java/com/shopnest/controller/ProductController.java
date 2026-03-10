package com.shopnest.controller;

import com.shopnest.dto.request.CreateProductRequest;
import com.shopnest.dto.request.UpdateProductRequest;
import com.shopnest.dto.response.ProductResponse;
import com.shopnest.service.ProductService;
import com.shopnest.util.ApiResponse;
import com.shopnest.util.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    // PUBLIC
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getAllProducts(page, size, sortBy)));
    }

    // PUBLIC
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getProductById(id)));
    }

    // PUBLIC
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductsByCategory(categoryId, page, size)));
    }

    // PUBLIC
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.searchProducts(keyword, page, size)));
    }

    // PUBLIC
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductsByPriceRange(min, max, page, size)));
    }

    // ADMIN, SELLER only
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully",
                        productService.createProduct(request)));
    }

    // ADMIN, SELLER only
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully",
                        productService.updateProduct(id, request)));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully", null));
    }

    // ADMIN, SELLER only
    @PatchMapping("/{id}/restock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> restockProduct(
            @PathVariable Long id,
            @RequestParam @Min(1) int quantity) {
        return ResponseEntity.ok(
                ApiResponse.success("Product restocked successfully",
                        productService.restockProduct(id, quantity)));
    }
}