package marcomanfrin.softwareops.DTO.clients;

import marcomanfrin.softwareops.enums.ClientType;

import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        ClientType type
) {
}
