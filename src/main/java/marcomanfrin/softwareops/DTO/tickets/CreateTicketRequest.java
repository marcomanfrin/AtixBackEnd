package marcomanfrin.softwareops.DTO.tickets;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateTicketRequest(
        @NotBlank String name,
        String description,
        UUID clientId,
        UUID plantId
) {
}
