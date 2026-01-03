package marcomanfrin.atixbackend.DTO.users;

import java.util.UUID;

public record UserSummaryDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String profileImageUrl,
        String role,
        String type
) {
}
