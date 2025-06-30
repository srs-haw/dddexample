package de.haw.swa.ordermanagement.domain.model.order;

import de.haw.swa.ordermanagement.domain.model.order.events.OrderConfirmed;
import de.haw.swa.ordermanagement.domain.model.order.events.OrderCreated;
import de.haw.swa.ordermanagement.domain.model.order.events.OrderPaid;
import de.haw.swa.ordermanagement.domain.model.order.events.OrderShipped;
import de.haw.swa.ordermanagement.domain.model.shared.DomainEvent;
import de.haw.swa.ordermanagement.domain.model.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    
    private Long orderId;
    private Long customerId;
    private List<OrderItem> orderItems;
    
    @BeforeEach
    void setUp() {
        orderId = 1L;
        customerId = 1L;
        
        OrderItem item1 = new OrderItem(
            1L,
            "Test Product 1",
            Money.euro(10.00),
            2
        );
        
        OrderItem item2 = new OrderItem(
            2L,
            "Test Product 2",
            Money.euro(15.00),
            1
        );
        
        orderItems = List.of(item1, item2);
    }
    
    @Test
    void shouldCreateOrderWithPendingStatus() {
        Order order = new Order(orderId, customerId, orderItems);
        
        assertEquals(orderId, order.getId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(2, order.getItems().size());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }
    
    @Test
    void shouldCalculateTotalAmountCorrectly() {
        Order order = new Order(orderId, customerId, orderItems);
        
        Money expectedTotal = Money.euro(35.00); // (10*2) + (15*1)
        assertEquals(expectedTotal, order.getTotalAmount());
    }
    
    @Test
    void shouldThrowExceptionWhenCreatingOrderWithoutItems() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Order(orderId, customerId, List.of());
        });
    }
    
    @Test
    void shouldConfirmPendingOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        
        order.confirm();
        
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }
    
    @Test
    void shouldThrowExceptionWhenConfirmingNonPendingOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        
        assertThrows(IllegalStateException.class, order::confirm);
    }
    
    @Test
    void shouldMarkConfirmedOrderAsPaid() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        
        order.markAsPaid();
        
        assertEquals(OrderStatus.PAID, order.getStatus());
    }
    
    @Test
    void shouldThrowExceptionWhenMarkingNonConfirmedOrderAsPaid() {
        Order order = new Order(orderId, customerId, orderItems);
        
        assertThrows(IllegalStateException.class, order::markAsPaid);
    }
    
    @Test
    void shouldShipPaidOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        order.markAsPaid();
        
        order.ship();
        
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }
    
    @Test
    void shouldThrowExceptionWhenShippingNonPaidOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        
        assertThrows(IllegalStateException.class, order::ship);
    }
    
    @Test
    void shouldDeliverShippedOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        order.markAsPaid();
        order.ship();
        
        order.deliver();
        
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }
    
    @Test
    void shouldThrowExceptionWhenDeliveringNonShippedOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        order.markAsPaid();
        
        assertThrows(IllegalStateException.class, order::deliver);
    }
    
    @Test
    void shouldCancelPendingOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        
        order.cancel();
        
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }
    
    @Test
    void shouldThrowExceptionWhenCancellingDeliveredOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        order.markAsPaid();
        order.ship();
        order.deliver();
        
        assertThrows(IllegalStateException.class, order::cancel);
    }
    
    @Test
    void shouldReturnDeliveredOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        order.markAsPaid();
        order.ship();
        order.deliver();
        
        order.returnOrder();
        
        assertEquals(OrderStatus.RETURNED, order.getStatus());
    }
    
    @Test
    void shouldThrowExceptionWhenReturningNonDeliveredOrder() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        order.markAsPaid();
        order.ship();
        
        assertThrows(IllegalStateException.class, order::returnOrder);
    }
    
    @Test
    void shouldRegisterOrderCreatedEvent() {
        Order order = new Order(orderId, customerId, orderItems);
        
        List<DomainEvent> events = order.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof OrderCreated);
        
        OrderCreated event = (OrderCreated) events.get(0);
        assertEquals(orderId, event.orderId());
        assertEquals(customerId, event.customerId());
    }
    
    @Test
    void shouldRegisterOrderConfirmedEvent() {
        Order order = new Order(orderId, customerId, orderItems);
        order.clearEvents();
        
        order.confirm();
        
        List<DomainEvent> events = order.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof OrderConfirmed);
        
        OrderConfirmed event = (OrderConfirmed) events.get(0);
        assertEquals(orderId, event.orderId());
    }
    
    @Test
    void shouldRegisterOrderPaidEvent() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        order.clearEvents();
        
        order.markAsPaid();
        
        List<DomainEvent> events = order.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof OrderPaid);
        
        OrderPaid event = (OrderPaid) events.get(0);
        assertEquals(orderId, event.orderId());
    }
    
    @Test
    void shouldRegisterOrderShippedEvent() {
        Order order = new Order(orderId, customerId, orderItems);
        order.confirm();
        order.markAsPaid();
        order.clearEvents();
        
        order.ship();
        
        List<DomainEvent> events = order.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof OrderShipped);
        
        OrderShipped event = (OrderShipped) events.get(0);
        assertEquals(orderId, event.orderId());
    }
}