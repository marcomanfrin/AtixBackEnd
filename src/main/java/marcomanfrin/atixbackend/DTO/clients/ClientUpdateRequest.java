package marcomanfrin.atixbackend.DTO.clients;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import marcomanfrin.atixbackend.enums.ClientType;

public record ClientUpdateRequest(
        @Size(min = 2, max = 100, message = "Client name must be between 2 and 100 characters")
        String name,

        ClientType type
) {
}
