package de.haw.swa.ordermanagement.application.eventhandler;

import de.haw.swa.ordermanagement.application.service.OrderService;
import de.haw.swa.ordermanagement.domain.model.order.events.OrderPaid;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderPaidEventHandler {
    
    private final OrderService orderService;
    
    public OrderPaidEventHandler(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderPaid(OrderPaid event) {
        // Automatically trigger shipment when an order has been paid
        orderService.shipOrder(event.orderId());
    }
}