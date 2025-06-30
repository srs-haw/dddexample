package de.haw.swa.ordermanagement.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to create a new product")
public record CreateProductRequest(
    @NotBlank
    @Schema(description = "Name of the product", example = "Gaming Laptop", required = true)
    String name,
    
    @Schema(description = "Detailed description of the product", example = "High-performance gaming laptop with RTX graphics")
    String description,
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Price of the product in EUR", example = "1299.99", required = true)
    BigDecimal price,
    
    @NotNull
    @Min(0)
    @Schema(description = "Initial stock quantity", example = "50", required = true)
    Integer stockQuantity
) {}