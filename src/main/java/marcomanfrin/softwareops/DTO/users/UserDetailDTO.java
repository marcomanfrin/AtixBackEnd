package marcomanfrin.softwareops.DTO.users;

import java.util.UUID;

public record UserDetailDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String profileImageUrl,
        String role
) {
}
