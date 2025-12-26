package marcomanfrin.softwareops.ServiceInterfaces;

import marcomanfrin.softwareops.DTO.auth.RegisterRequest;
import marcomanfrin.softwareops.DTO.users.UserDetailDTO;
import marcomanfrin.softwareops.DTO.users.UserSummaryDTO;
import marcomanfrin.softwareops.entities.users.User;
import marcomanfrin.softwareops.enums.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserService {
    UserDetailDTO registerUser(RegisterRequest request);
    Optional<User> getUserByEmail(String email);
    List<UserSummaryDTO> getAllUsers();
    UserDetailDTO getUserById(UUID id);
    void updatePassword(UUID userId, String currentPassword, String newPassword);
    void updateProfileImage(UUID userId, String imageUrl);
    List<User> getUsersByRole(UserRole role);
}
