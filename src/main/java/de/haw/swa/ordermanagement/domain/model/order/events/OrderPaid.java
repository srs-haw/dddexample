package de.haw.swa.ordermanagement.domain.model.order.events;

import de.haw.swa.ordermanagement.domain.model.shared.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event indicating that an order has been paid.
 * This event triggers automatic shipping processing.
 */
public record OrderPaid(
        UUID eventId,
        LocalDateTime occurredOn,
        Long orderId
) implements DomainEvent {
    
    /**
     * Factory method to create OrderPaid event with auto-generated metadata.
     */
    public static OrderPaid create(Long orderId) {
        var eventData = DomainEvent.createEventData();
        return new OrderPaid(eventData.eventId(), eventData.occurredOn(), orderId);
    }
}