package de.haw.swa.ordermanagement.interfaces.rest;

import de.haw.swa.ordermanagement.application.service.OrderService;
import de.haw.swa.ordermanagement.domain.model.order.Order;
import de.haw.swa.ordermanagement.domain.model.order.OrderStatus;
import de.haw.swa.ordermanagement.interfaces.rest.dto.CreateOrderRequest;
import de.haw.swa.ordermanagement.interfaces.rest.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Orders", description = "Order lifecycle management operations")
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order with the specified customer and product items. The order starts in PENDING status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class),
                examples = @ExampleObject(
                    name = "order_created",
                    summary = "Created order example",
                    value = """
                        {
                          "orderId": "1",
                          "customerId": "1",
                          "status": "PENDING",
                          "totalAmount": 2599.98,
                          "currency": "EUR",
                          "items": [
                            {
                              "productId": "1",
                              "productName": "Gaming Laptop",
                              "quantity": 2,
                              "unitPrice": 1299.99,
                              "totalPrice": 2599.98
                            }
                          ],
                          "createdAt": "2024-01-15T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid order data provided",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Order creation request",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateOrderRequest.class),
                    examples = @ExampleObject(
                        name = "create_order",
                        summary = "Create order example",
                        value = """
                            {
                              "customerId": "1",
                              "items": [
                                {
                                  "productId": "1",
                                  "quantity": 2
                                },
                                {
                                  "productId": "2",
                                  "quantity": 1
                                }
                              ]
                            }
                            """
                    )
                )
            )
            CreateOrderRequest request) {
        List<OrderService.OrderItemDto> items = request.items().stream()
            .map(item -> new OrderService.OrderItemDto(
                item.productId(),
                item.quantity()
            ))
            .toList();
        
        Order order = orderService.createOrder(
            request.customerId(),
            items
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(OrderResponse.from(order));
    }
    
    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves a specific order by its unique identifier."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order found and retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found",
            content = @Content()
        )
    })
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Unique identifier of the order", example = "1", required = true)
            @PathVariable Long orderId) {
        return orderService.findById(orderId)
            .map(order -> ResponseEntity.ok(OrderResponse.from(order)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(
        summary = "Get orders with optional filtering",
        description = "Retrieves orders with optional filtering by customer ID or order status. If no filters are provided, returns all orders."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "array", implementation = OrderResponse.class)
            )
        )
    })
    public ResponseEntity<List<OrderResponse>> getOrders(
            @Parameter(description = "Filter orders by customer ID", example = "1")
            @RequestParam(required = false) Long customerId,
            @Parameter(description = "Filter orders by status", example = "PENDING")
            @RequestParam(required = false) OrderStatus status) {
        
        List<Order> orders;
        
        if (customerId != null) {
            orders = orderService.findByCustomerId(customerId);
        } else if (status != null) {
            orders = orderService.findByStatus(status);
        } else {
            orders = orderService.findAll(); // Return all orders
        }
        
        List<OrderResponse> response = orders.stream()
            .map(OrderResponse::from)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{orderId}/confirm")
    @Operation(
        summary = "Confirm order",
        description = "Confirms a pending order, transitioning it from PENDING to CONFIRMED status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order confirmed successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid order status for confirmation")
    })
    public ResponseEntity<Void> confirmOrder(
            @Parameter(description = "Unique identifier of the order to confirm", example = "1", required = true)
            @PathVariable Long orderId) {
        orderService.confirmOrder(orderId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{orderId}/pay")
    @Operation(
        summary = "Process payment for order",
        description = """
            Processes payment for the specified order, transitioning it from CONFIRMED to PAID status.
            
            **Important**: When payment is processed, the system automatically publishes an OrderPaid event,
            which triggers automatic shipping via the OrderPaidEventHandler. The order will be automatically
            shipped without manual intervention.
            
            **Event-driven flow**: CONFIRMED → PAID → (automatic) SHIPPED
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment processed successfully, order automatically shipped",
            content = @Content()
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found",
            content = @Content()
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid order status for payment processing",
            content = @Content()
        )
    })
    public ResponseEntity<Void> payOrder(
            @Parameter(description = "Unique identifier of the order to process payment for", example = "1", required = true)
            @PathVariable Long orderId) {
        orderService.processPayment(orderId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{orderId}/ship")
    @Operation(
        summary = "Ship order (manual override)",
        description = """
            Manually ships an order. Note: In most cases, orders are automatically shipped when payment is processed.
            This endpoint is available for manual override scenarios or orders that bypass automatic shipping.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order shipped successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid order status for shipping")
    })
    public ResponseEntity<Void> shipOrder(
            @Parameter(description = "Unique identifier of the order to ship", example = "1", required = true)
            @PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{orderId}/deliver")
    @Operation(
        summary = "Mark order as delivered",
        description = "Marks a shipped order as delivered, transitioning it from SHIPPED to DELIVERED status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order marked as delivered successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid order status for delivery")
    })
    public ResponseEntity<Void> deliverOrder(
            @Parameter(description = "Unique identifier of the order to mark as delivered", example = "1", required = true)
            @PathVariable Long orderId) {
        orderService.deliverOrder(orderId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{orderId}/cancel")
    @Operation(
        summary = "Cancel order",
        description = "Cancels an order. Orders can be cancelled from PENDING, CONFIRMED, or PAID status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid order status for cancellation")
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Unique identifier of the order to cancel", example = "1", required = true)
            @PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{orderId}/return")
    @Operation(
        summary = "Return order",
        description = "Processes a return for a delivered order, transitioning it from DELIVERED to RETURNED status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order return processed successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid order status for return")
    })
    public ResponseEntity<Void> returnOrder(
            @Parameter(description = "Unique identifier of the order to return", example = "1", required = true)
            @PathVariable Long orderId) {
        orderService.returnOrder(orderId);
        return ResponseEntity.ok().build();
    }
}