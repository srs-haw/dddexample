package de.haw.swa.ordermanagement.interfaces.rest.dto;

import de.haw.swa.ordermanagement.domain.model.order.Order;
import de.haw.swa.ordermanagement.domain.model.order.OrderItem;
import de.haw.swa.ordermanagement.domain.model.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order information response")
public record OrderResponse(
    @Schema(description = "Unique identifier of the order", example = "1")
    String orderId,
    
    @Schema(description = "ID of the customer who placed the order", example = "1")
    String customerId,
    
    @Schema(description = "List of items in the order")
    List<OrderItemResponse> items,
    
    @Schema(description = "Total amount of the order", example = "2599.98")
    BigDecimal totalAmount,
    
    @Schema(description = "Currency code", example = "EUR")
    String currency,
    
    @Schema(description = "Current status of the order", example = "PENDING")
    OrderStatus status,
    
    @Schema(description = "Date and time when the order was created", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "Date and time when the order was last updated", example = "2024-01-15T10:30:00")
    LocalDateTime updatedAt
) {
    
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId().toString(),
            order.getCustomerId().toString(),
            order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList(),
            order.getTotalAmount().getAmount(),
            order.getTotalAmount().getCurrency().getCurrencyCode(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
    
    @Schema(description = "Order item information")
    public record OrderItemResponse(
        @Schema(description = "ID of the product", example = "1")
        String productId,
        
        @Schema(description = "Name of the product", example = "Gaming Laptop")
        String productName,
        
        @Schema(description = "Unit price of the product", example = "1299.99")
        BigDecimal unitPrice,
        
        @Schema(description = "Quantity ordered", example = "2")
        int quantity,
        
        @Schema(description = "Total price for this item (unit price × quantity)", example = "2599.98")
        BigDecimal totalPrice
    ) {
        
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                item.getProductId().toString(),
                item.getProductName(),
                item.getUnitPrice().getAmount(),
                item.getQuantity(),
                item.getTotalPrice().getAmount()
            );
        }
    }
}