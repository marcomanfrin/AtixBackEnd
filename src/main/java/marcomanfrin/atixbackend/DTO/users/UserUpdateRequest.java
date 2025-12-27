package marcomanfrin.atixbackend.DTO.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import marcomanfrin.atixbackend.enums.UserRole;

public record UserUpdateRequest(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Email(message = "Email must be valid")
        String email,

        UserRole role
) {
}
