package com.shopnest.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private int stockQty;

    @Pattern(
            regexp = "^(https?://).+",
            message = "Image URL must start with http:// or https://"
    )
    private String imageUrl;

    @Size(max = 50, message = "Type cannot exceed 50 characters")
    private String type;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long sellerId;
}