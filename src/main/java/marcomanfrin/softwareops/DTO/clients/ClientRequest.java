package marcomanfrin.softwareops.DTO.clients;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import marcomanfrin.softwareops.enums.ClientType;

public record ClientRequest(
        @NotBlank(message = "Client name is required")
        @Size(min = 2, max = 100, message = "Client name must be between 2 and 100 characters")
        String name,

        @NotNull(message = "Client type is required")
        ClientType type
) {
}
