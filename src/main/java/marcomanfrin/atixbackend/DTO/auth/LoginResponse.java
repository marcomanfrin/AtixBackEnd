package marcomanfrin.atixbackend.DTO.auth;

public record LoginResponse(
        String token,
        String email,
        String firstName,
        String lastName,
        String role
) {
}
