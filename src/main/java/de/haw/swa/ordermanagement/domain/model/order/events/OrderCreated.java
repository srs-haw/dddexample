package de.haw.swa.ordermanagement.domain.model.order.events;

import de.haw.swa.ordermanagement.domain.model.shared.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event indicating that an order has been created.
 * This event is fired when a new order is successfully created in the system.
 */
public record OrderCreated(
        UUID eventId,
        LocalDateTime occurredOn,
        Long orderId,
        Long customerId
) implements DomainEvent {
    
    /**
     * Factory method to create OrderCreated event with auto-generated metadata.
     */
    public static OrderCreated create(Long orderId, Long customerId) {
        var eventData = DomainEvent.createEventData();
        return new OrderCreated(eventData.eventId(), eventData.occurredOn(), orderId, customerId);
    }
}