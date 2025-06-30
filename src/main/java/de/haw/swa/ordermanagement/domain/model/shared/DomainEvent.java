package de.haw.swa.ordermanagement.domain.model.shared;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Marker interface for domain events in the DDD architecture.
 * Domain events represent something important that happened in the domain.
 * They are immutable and should be implemented as records.
 */
public interface DomainEvent {
    
    /**
     * Unique identifier for this event instance.
     */
    UUID eventId();
    
    /**
     * Timestamp when this event occurred.
     */
    LocalDateTime occurredOn();
    
    /**
     * Creates a new domain event with auto-generated ID and current timestamp.
     */
    static DomainEventData createEventData() {
        return new DomainEventData(UUID.randomUUID(), LocalDateTime.now());
    }
    
    /**
     * Helper record to provide common event data.
     */
    record DomainEventData(UUID eventId, LocalDateTime occurredOn) {}
}