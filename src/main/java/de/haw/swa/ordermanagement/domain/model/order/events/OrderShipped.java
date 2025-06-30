package de.haw.swa.ordermanagement.domain.model.order.events;

import de.haw.swa.ordermanagement.domain.model.shared.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event indicating that an order has been shipped.
 * This event is fired when an order moves from PAID to SHIPPED status.
 */
public record OrderShipped(
        UUID eventId,
        LocalDateTime occurredOn,
        Long orderId
) implements DomainEvent {
    
    /**
     * Factory method to create OrderShipped event with auto-generated metadata.
     */
    public static OrderShipped create(Long orderId) {
        var eventData = DomainEvent.createEventData();
        return new OrderShipped(eventData.eventId(), eventData.occurredOn(), orderId);
    }
}