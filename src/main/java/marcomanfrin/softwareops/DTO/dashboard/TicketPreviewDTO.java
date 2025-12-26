package marcomanfrin.softwareops.DTO.dashboard;

import marcomanfrin.softwareops.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketPreviewDTO(
        UUID id,
        String name,
        TicketStatus status,
        LocalDateTime createdAt
) {
}
