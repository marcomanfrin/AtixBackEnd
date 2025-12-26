package marcomanfrin.softwareops.DTO.tickets;

import marcomanfrin.softwareops.enums.TicketStatus;

import java.util.UUID;

public record TicketResponse(
        UUID id,
        String senderEmail,
        UUID orderNumberId,
        String name,
        String description,
        TicketStatus status
) {
}
