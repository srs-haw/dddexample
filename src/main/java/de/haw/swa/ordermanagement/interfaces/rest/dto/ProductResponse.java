package de.haw.swa.ordermanagement.interfaces.rest.dto;

import de.haw.swa.ordermanagement.domain.model.product.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Product information response")
public record ProductResponse(
    @Schema(description = "Unique identifier of the product", example = "1")
    String productId,
    
    @Schema(description = "Name of the product", example = "Gaming Laptop")
    String name,
    
    @Schema(description = "Detailed description of the product", example = "High-performance gaming laptop with RTX graphics")
    String description,
    
    @Schema(description = "Price of the product", example = "1299.99")
    BigDecimal price,
    
    @Schema(description = "Currency code", example = "EUR")
    String currency,
    
    @Schema(description = "Available stock quantity", example = "50")
    int stockQuantity
) {
    
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId().toString(),
            product.getName(),
            product.getDescription(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency().getCurrencyCode(),
            product.getStockQuantity()
        );
    }
}