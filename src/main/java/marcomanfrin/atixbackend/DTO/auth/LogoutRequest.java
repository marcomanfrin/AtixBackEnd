package marcomanfrin.atixbackend.DTO.auth;

import java.util.UUID;

public record LogoutRequest(UUID sessionId) {}
