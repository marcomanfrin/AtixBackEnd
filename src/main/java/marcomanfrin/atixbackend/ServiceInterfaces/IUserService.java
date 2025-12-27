package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.auth.RegisterRequest;
import marcomanfrin.atixbackend.DTO.users.UserDetailDTO;
import marcomanfrin.atixbackend.DTO.users.UserSummaryDTO;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.UserRole;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserService {
    UserDetailDTO registerUser(RegisterRequest request);
    Optional<User> getUserByEmail(String email);
    List<UserSummaryDTO> getAllUsers();
    UserDetailDTO getUserById(UUID id);
    User getUserEntityById(UUID id); // Internal use for security filters
    void updatePassword(UUID userId, String currentPassword, String newPassword);
    public String uploadProfileImage(UUID userId, MultipartFile file);
    List<User> getUsersByRole(UserRole role);
}
