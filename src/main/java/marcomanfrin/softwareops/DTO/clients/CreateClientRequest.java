package marcomanfrin.softwareops.DTO.clients;

import marcomanfrin.softwareops.enums.ClientType;

public record CreateClientRequest(String name, ClientType type) {
}
