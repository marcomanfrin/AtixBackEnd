package marcomanfrin.atixbackend.DTO.tickets;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import marcomanfrin.atixbackend.enums.TicketStatus;

import java.util.UUID;

public record TicketUpdateRequest(
        @Email(message = "Sender email must be valid")
        String senderEmail,

        UUID orderNumberId,

        @Size(min = 2, max = 100, message = "Ticket name must be between 2 and 100 characters")
        String name,

        String description,

        TicketStatus status
) {
}
