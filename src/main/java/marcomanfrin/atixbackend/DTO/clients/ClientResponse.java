package marcomanfrin.atixbackend.DTO.clients;

import marcomanfrin.atixbackend.enums.ClientType;

import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        ClientType type
) {
}
