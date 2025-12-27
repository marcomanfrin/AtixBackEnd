package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.auth.RegisterRequest;
import marcomanfrin.atixbackend.DTO.users.UserDetailDTO;
import marcomanfrin.atixbackend.DTO.users.UserSummaryDTO;
import marcomanfrin.atixbackend.DTO.users.UserUpdateRequest;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.UserType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserService {
    UserDetailDTO registerUser(RegisterRequest request);
    Optional<User> getUserByEmail(String email);
    List<UserSummaryDTO> getAllUsers();
    List<UserSummaryDTO> getUsersByType(UserType type);
    UserDetailDTO getUserById(UUID id);
    User getUserEntityById(UUID id); // Internal use for security filters
    UserDetailDTO updateUser(UUID id, UserUpdateRequest request);
    void deleteUser(UUID id);
    void updatePassword(UUID userId, String currentPassword, String newPassword);
    String uploadProfileImage(UUID userId, MultipartFile file);
    List<User> getUsersByRole(UserRole role);
}
