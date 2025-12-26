package marcomanfrin.softwareops.DTO.tickets;

import marcomanfrin.softwareops.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String name,
        String description,
        TicketStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt,
        UUID clientId,
        UUID plantId
) {}