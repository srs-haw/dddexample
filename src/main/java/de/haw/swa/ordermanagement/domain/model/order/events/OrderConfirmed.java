package de.haw.swa.ordermanagement.domain.model.order.events;

import de.haw.swa.ordermanagement.domain.model.shared.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event indicating that an order has been confirmed.
 * This event is fired when an order moves from PENDING to CONFIRMED status.
 */
public record OrderConfirmed(
        UUID eventId,
        LocalDateTime occurredOn,
        Long orderId
) implements DomainEvent {
    
    /**
     * Factory method to create OrderConfirmed event with auto-generated metadata.
     */
    public static OrderConfirmed create(Long orderId) {
        var eventData = DomainEvent.createEventData();
        return new OrderConfirmed(eventData.eventId(), eventData.occurredOn(), orderId);
    }
}