package marcomanfrin.atixbackend.DTO.dashboard;

import marcomanfrin.atixbackend.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketPreviewDTO(
        UUID id,
        String name,
        TicketStatus status,
        LocalDateTime createdAt
) {
}
