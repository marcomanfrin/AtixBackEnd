package marcomanfrin.softwareops.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.persistence.EntityNotFoundException;
import marcomanfrin.softwareops.DTO.users.ChangePasswordRequest;
import marcomanfrin.softwareops.DTO.registration.NewUserDTO;
import marcomanfrin.softwareops.DTO.registration.NewUserRespDTO;
import marcomanfrin.softwareops.DTO.users.UpdateUserRequest;
import marcomanfrin.softwareops.entities.AdministrativeUser;
import marcomanfrin.softwareops.entities.TechnicianUser;
import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.enums.UserRole;
import marcomanfrin.softwareops.enums.UserType;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.exceptions.UnauthorizedException;
import marcomanfrin.softwareops.repositories.UserRepository;
import marcomanfrin.softwareops.tools.MailgunSender;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final Cloudinary imageUploader;
    private final MailgunSender mailgunSender;
    private final PasswordEncoder bcrypt;

    public UserService(UserRepository userRepository, Cloudinary imageUploader, MailgunSender mailgunSender, PasswordEncoder bcrypt) {
        this.userRepository = userRepository;
        this.imageUploader = imageUploader;
        this.mailgunSender = mailgunSender;
        this.bcrypt = bcrypt;
    }

    @Override
    public NewUserRespDTO saveNewUser(NewUserDTO body) {
        String u = requireNotBlank(body.userName(), "username");
        String e = requireNotBlank(body.email(), "email").toLowerCase();
        String p = requireNotBlank(body.password(), "password");

        if (userRepository.existsByUsernameIgnoreCase(u)) {
            throw new DataIntegrityViolationException("Username already exists: " + u);
        }
        if (userRepository.existsByEmailIgnoreCase(e)) {
            throw new DataIntegrityViolationException("Email already exists: " + e);
        }

        User user = createUserByType(body.userType());

        user.setFirstName(body.firstName());
        user.setLastName(body.lastName());
        user.setUsername(u);
        user.setEmail(e);
        user.setPasswordHash(bcrypt.encode(p));
        user.setRole(body.role());
        user.setProfileImageUrl("https://ui-avatars.com/api/?name=" + body.firstName() + "+" + body.lastName());
        User saved = userRepository.save(user);
        this.mailgunSender.sendRegistrationEmail(saved);
        return new NewUserRespDTO(saved.getId());
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        if (req == null) throw new IllegalArgumentException("Request cannot be null");

        String oldPw = requireNotBlank(req.oldPassword(), "oldPassword");
        String newPw = requireNotBlank(req.newPassword(), "newPassword");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (!bcrypt.matches(oldPw, user.getPassword())) {
            throw new UnauthorizedException("Old password is incorrect");
        }

        user.setPasswordHash(bcrypt.encode(newPw));
        userRepository.save(user);
    }

    private User createUserByType(UserType userType) {
        return switch (userType) {
            case ADMINISTRATIVE -> new AdministrativeUser();
            case TECHNICIAN -> new TechnicianUser();
        };
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User changeUserRole(UUID id, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfileImageUrl(UUID userId, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        user.setProfileImageUrl(requireNotBlank(profileImageUrl, "profileImageUrl"));
        return userRepository.save(user);
    }



    @Override
    public List<TechnicianUser> getTechnicians() {
        return userRepository.findAllTechnicians();
    }

    @Override
    public List<User> getUsersByRole(UserRole role) {
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        return userRepository.findByRole(role);
    }

    @Override
    public String uploadProfileImage(UUID userId, MultipartFile file) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        Map<String, Object> options = ObjectUtils.asMap(
                "folder", "SoftwareOps/avatars",
                "public_id", userId.toString()
        );

        try {
            Map<?, ?> result = imageUploader.uploader().upload(
                    file.getBytes(),
                    options
            );

            String imageUrl = result.get("secure_url").toString();

            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);

            return imageUrl;

        } catch (IOException e) {
            throw new RuntimeException("Error uploading image", e);
        }
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmail(email);
    }



    private String requireNotBlank(String value, String field) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value.trim();
    }
}
