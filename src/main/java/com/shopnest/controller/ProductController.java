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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // GET /api/products?page=0&size=10&sortBy=name
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getAllProducts(page, size, sortBy)));
    }

    // GET /api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getProductById(id)));
    }

    // ── IMPORTANT: specific routes BEFORE generic ─────
// GET /api/products/category/slug/mobiles
    @GetMapping("/category/slug/{slug}")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getByCategorySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductsByCategorySlug(slug, page, size)));
    }

    // GET /api/products/category/13
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getProductsByCategory(categoryId, page, size)));
    }

    // GET /api/products/search?keyword=iphone&page=0&size=10
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.searchProducts(keyword, page, size)));
    }

    // GET /api/products/price-range?min=1000&max=50000
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

    // POST /api/products
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)    // ← must be here
                .body(ApiResponse.success("Product created successfully",
                        productService.createProduct(request)));
    }

    // PUT /api/products/1
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully",
                        productService.updateProduct(id, request)));
    }

    // DELETE /api/products/1
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully", null));
    }

    // PATCH /api/products/1/restock?quantity=50
    @PatchMapping("/{id}/restock")
    public ResponseEntity<ApiResponse<ProductResponse>> restockProduct(
            @PathVariable Long id,
            @RequestParam @Min(1) int quantity) {
        return ResponseEntity.ok(
                ApiResponse.success("Product restocked successfully",
                        productService.restockProduct(id, quantity)));
    }
}