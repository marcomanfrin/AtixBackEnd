package marcomanfrin.atixbackend.DTO.tickets;

import marcomanfrin.atixbackend.enums.TicketStatus;

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
