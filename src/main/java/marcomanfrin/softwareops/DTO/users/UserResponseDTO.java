package marcomanfrin.softwareops.DTO.users;

import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.enums.UserRole;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        String profileImageUrl
) {

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getProfileImageUrl()
        );
    }
}