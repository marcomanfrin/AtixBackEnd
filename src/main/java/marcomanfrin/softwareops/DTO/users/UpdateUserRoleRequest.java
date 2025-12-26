package marcomanfrin.softwareops.DTO.users;

import marcomanfrin.softwareops.enums.UserRole;

public record UpdateUserRoleRequest(UserRole role) {
}
