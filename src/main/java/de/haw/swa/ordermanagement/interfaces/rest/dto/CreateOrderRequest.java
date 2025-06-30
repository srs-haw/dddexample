package de.haw.swa.ordermanagement.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request to create a new order")
public record CreateOrderRequest(
    @NotNull
    @Schema(description = "ID of the customer placing the order", example = "1", required = true)
    Long customerId,
    
    @NotEmpty
    @Valid
    @Schema(description = "List of products and quantities to order", required = true)
    List<OrderItemRequest> items
) {
    
    @Schema(description = "Individual item in the order")
    public record OrderItemRequest(
        @NotNull
        @Schema(description = "ID of the product to order", example = "1", required = true)
        Long productId,
        
        @NotNull
        @Schema(description = "Quantity of the product to order", example = "2", required = true)
        Integer quantity
    ) {}
}