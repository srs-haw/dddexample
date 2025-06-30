package de.haw.swa.ordermanagement.application.eventhandler;

import de.haw.swa.ordermanagement.application.service.OrderService;
import de.haw.swa.ordermanagement.domain.model.order.events.OrderPaid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderPaidEventHandlerTest {
    
    @Mock
    private OrderService orderService;
    
    private OrderPaidEventHandler eventHandler;
    
    @BeforeEach
    void setUp() {
        eventHandler = new OrderPaidEventHandler(orderService);
    }
    
    @Test
    void shouldTriggerShipmentWhenOrderPaidEventIsReceived() {
        // Given
        Long orderId = 1L;
        OrderPaid event = OrderPaid.create(orderId);
        
        // When
        eventHandler.handleOrderPaid(event);
        
        // Then
        verify(orderService).shipOrder(orderId);
    }
}