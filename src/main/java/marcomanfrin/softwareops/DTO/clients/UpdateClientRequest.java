package marcomanfrin.softwareops.DTO.clients;

import marcomanfrin.softwareops.enums.ClientType;

public record UpdateClientRequest(String name, ClientType type) {
}
