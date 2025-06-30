package de.haw.swa.ordermanagement.application.service;

import de.haw.swa.ordermanagement.domain.model.order.Order;
import de.haw.swa.ordermanagement.domain.model.order.OrderItem;
import de.haw.swa.ordermanagement.domain.model.order.OrderStatus;
import de.haw.swa.ordermanagement.domain.model.product.Product;
import de.haw.swa.ordermanagement.domain.model.shared.Money;
import de.haw.swa.ordermanagement.domain.repository.OrderRepository;
import de.haw.swa.ordermanagement.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import de.haw.swa.ordermanagement.domain.model.order.events.OrderPaid;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private ShippingService shippingService;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    private OrderService orderService;
    
    private Long customerId;
    private Long productId;
    private Product product;
    
    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository, paymentService, shippingService, eventPublisher);
        
        customerId = 1L;
        productId = 1L;
        product = new Product(productId, "Test Product", "Description", Money.euro(10.00), 5);
    }
    
    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        List<OrderService.OrderItemDto> orderItems = List.of(
            new OrderService.OrderItemDto(productId, 2)
        );
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Order result = orderService.createOrder(customerId, orderItems);
        
        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(1, result.getItems().size());
        assertEquals(Money.euro(20.00), result.getTotalAmount());
        
        verify(productRepository).findById(productId);
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        List<OrderService.OrderItemDto> orderItems = List.of(
            new OrderService.OrderItemDto(productId, 2)
        );
        
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(customerId, orderItems);
        });
        
        verify(productRepository).findById(productId);
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given
        List<OrderService.OrderItemDto> orderItems = List.of(
            new OrderService.OrderItemDto(productId, 10) // More than available stock (5)
        );
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(customerId, orderItems);
        });
        
        verify(productRepository).findById(productId);
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void shouldConfirmOrderAndReduceStock() {
        // Given
        Long orderId = 1L;
        Order order = createTestOrder(orderId);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        orderService.confirmOrder(orderId);
        
        // Then
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(3, product.getStockQuantity()); // 5 - 2 = 3
        
        verify(orderRepository).findById(orderId);
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }
    
    @Test
    void shouldProcessPaymentSuccessfully() {
        // Given
        Long orderId = 1L;
        Order order = createTestOrder(orderId);
        order.confirm();
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(any(Money.class))).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        orderService.processPayment(orderId);
        
        // Then
        assertEquals(OrderStatus.PAID, order.getStatus());
        
        verify(orderRepository).findById(orderId);
        verify(paymentService).processPayment(order.getTotalAmount());
        verify(orderRepository).save(order);
        verify(eventPublisher).publishEvent(any(OrderPaid.class));
    }
    
    @Test
    void shouldThrowExceptionWhenPaymentFails() {
        // Given
        Long orderId = 1L;
        Order order = createTestOrder(orderId);
        order.confirm();
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(any(Money.class))).thenReturn(false);
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            orderService.processPayment(orderId);
        });
        
        verify(orderRepository).findById(orderId);
        verify(paymentService).processPayment(order.getTotalAmount());
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void shouldShipOrderSuccessfully() {
        // Given
        Long orderId = 1L;
        Order order = createTestOrder(orderId);
        order.confirm();
        order.markAsPaid();
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(shippingService.createShipment(order)).thenReturn("TRACK-12345");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        orderService.shipOrder(orderId);
        
        // Then
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        
        verify(orderRepository).findById(orderId);
        verify(shippingService).createShipment(order);
        verify(orderRepository).save(order);
    }
    
    @Test
    void shouldCancelOrderAndReturnStock() {
        // Given
        Long orderId = 1L;
        Order order = createTestOrder(orderId);
        order.confirm();
        product.reduceStock(2); // Simulate stock reduction
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        orderService.cancelOrder(orderId);
        
        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(5, product.getStockQuantity()); // Stock returned
        
        verify(orderRepository).findById(orderId);
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }
    
    private Order createTestOrder(Long orderId) {
        List<OrderItem> orderItems = List.of(
            new OrderItem(productId, product.getName(), product.getPrice(), 2)
        );
        
        return new Order(orderId, customerId, orderItems);
    }
}