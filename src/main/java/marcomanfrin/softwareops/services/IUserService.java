package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.users.ChangePasswordRequest;
import marcomanfrin.softwareops.DTO.registration.NewUserDTO;
import marcomanfrin.softwareops.DTO.registration.NewUserRespDTO;
import marcomanfrin.softwareops.DTO.users.UpdateUserRequest;
import marcomanfrin.softwareops.entities.TechnicianUser;
import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface IUserService {
    Optional<User> getUserById(UUID id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllUsers();
    Page<User> getAllUsers(Pageable pageable);
    User updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
    User changeUserRole(UUID id, UserRole role);
    User updateProfileImageUrl(UUID userId, String profileImageUrl);
    void changePassword(UUID userId, ChangePasswordRequest req);
    List<TechnicianUser> getTechnicians();
    List<User> getUsersByRole(UserRole role);
    String uploadProfileImage(UUID userId, MultipartFile file);
    Optional<User> getUserByEmail(String email);

    NewUserRespDTO saveNewUser(NewUserDTO body);
}