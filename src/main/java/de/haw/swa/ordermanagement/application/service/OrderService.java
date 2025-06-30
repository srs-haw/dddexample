package de.haw.swa.ordermanagement.application.service;

import de.haw.swa.ordermanagement.domain.model.order.*;
import de.haw.swa.ordermanagement.domain.model.product.Product;
import de.haw.swa.ordermanagement.domain.repository.OrderRepository;
import de.haw.swa.ordermanagement.domain.repository.ProductRepository;
import de.haw.swa.ordermanagement.domain.model.shared.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final ApplicationEventPublisher eventPublisher;
    
    public OrderService(OrderRepository orderRepository, 
                       ProductRepository productRepository,
                       PaymentService paymentService,
                       ShippingService shippingService,
                       ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.eventPublisher = eventPublisher;
    }
    
    public Order createOrder(Long customerId, List<OrderItemDto> orderItems) {
        // Check stock availability and create items in one pass
        List<OrderItem> items = orderItems.stream()
            .map(itemDto -> {
                Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemDto.productId()));
                
                if (!product.isAvailable(itemDto.quantity())) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }
                
                return new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    itemDto.quantity()
                );
            })
            .toList();
        
        Order order = new Order(customerId, items);
        Order savedOrder = orderRepository.save(order);
        
        // Register creation event after persisting (when ID is available)
        savedOrder.registerCreationEvent();
        publishDomainEvents(savedOrder);
        
        return savedOrder;
    }
    
    public void confirmOrder(Long orderId) {
        Order order = findOrderById(orderId);
        
        // Reserve stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            
            product.reduceStock(item.getQuantity());
            productRepository.save(product);
        }
        
        order.confirm();
        orderRepository.save(order);
        publishDomainEvents(order);
    }
    
    public void processPayment(Long orderId) {
        Order order = findOrderById(orderId);
        
        boolean paymentSuccessful = paymentService.processPayment(order.getTotalAmount());
        
        if (paymentSuccessful) {
            order.markAsPaid();
            orderRepository.save(order);
            publishDomainEvents(order);
        } else {
            throw new IllegalStateException("Payment processing failed for order: " + orderId);
        }
    }
    
    public void shipOrder(Long orderId) {
        Order order = findOrderById(orderId);
        
        String trackingNumber = shippingService.createShipment(order);
        
        order.ship();
        orderRepository.save(order);
        publishDomainEvents(order);
    }
    
    public void deliverOrder(Long orderId) {
        Order order = findOrderById(orderId);
        order.deliver();
        orderRepository.save(order);
        publishDomainEvents(order);
    }
    
    public void cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
        
        // Return stock if order was confirmed
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
                
                product.increaseStock(item.getQuantity());
                productRepository.save(product);
            }
        }
        
        order.cancel();
        orderRepository.save(order);
        publishDomainEvents(order);
    }
    
    public void returnOrder(Long orderId) {
        Order order = findOrderById(orderId);
        
        // Return stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }
        
        order.returnOrder();
        orderRepository.save(order);
        publishDomainEvents(order);
    }
    
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
    
    private void publishDomainEvents(Order order) {
        List<DomainEvent> events = order.getDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        order.clearEvents();
    }
    
    public record OrderItemDto(Long productId, int quantity) {}
}