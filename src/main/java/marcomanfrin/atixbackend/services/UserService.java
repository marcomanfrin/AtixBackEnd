package marcomanfrin.atixbackend.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.persistence.EntityNotFoundException;
import marcomanfrin.atixbackend.DTO.auth.RegisterRequest;
import marcomanfrin.atixbackend.DTO.users.UserDetailDTO;
import marcomanfrin.atixbackend.DTO.users.UserSummaryDTO;
import marcomanfrin.atixbackend.DTO.users.UserUpdateRequest;
import marcomanfrin.atixbackend.ServiceInterfaces.IUserService;
import marcomanfrin.atixbackend.entities.users.AdministrativeUser;
import marcomanfrin.atixbackend.entities.users.SellerUser;
import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.UserType;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.exceptions.UnauthorizedException;
import marcomanfrin.atixbackend.exceptions.ValidationException;
import marcomanfrin.atixbackend.repositories.UserRepository;
import marcomanfrin.atixbackend.repositories.WorkRepository;
import marcomanfrin.atixbackend.tools.MailgunSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary imageUploader;
    private final MailgunSender mailgunSender;

    public UserService(UserRepository userRepository, WorkRepository workRepository, PasswordEncoder passwordEncoder, Cloudinary imageUploader, MailgunSender mailgunSender) {
        this.userRepository = userRepository;
        this.workRepository = workRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageUploader = imageUploader;
        this.mailgunSender = mailgunSender;
    }

    @Override
    @Transactional
    public UserDetailDTO registerUser(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ValidationException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = createUserByType(request, hashedPassword);

        User savedUser = userRepository.save(user);
        this.mailgunSender.sendRegistrationEmail(savedUser);
        return toUserDetailDTO(savedUser);
    }

    @Transactional
    public User createUserWithoutEmail(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ValidationException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = createUserByType(request, hashedPassword);
        return userRepository.save(user);
    }

    private User createUserByType(RegisterRequest request, String hashedPassword) {
        User user = switch (request.type()) {
            case ADMINISTRATION -> new AdministrativeUser();
            case TECHNICIAN -> new TechnicianUser();
            case SELLER -> new SellerUser();
        };

        user.setEmail(request.email());
        user.setPasswordHash(hashedPassword);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role());
        user.setProfileImageUrl("https://ui-avatars.com/api/?name=" + request.firstName() + "+" + request.lastName());

        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserSummaryDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSummaryDTO> getUsersByType(UserType type) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUserType() == type)
                .map(this::toUserSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("@securityService.isSelf(#id, authentication)")
    public UserDetailDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return toUserDetailDTO(user);
    }

    @Override
    public User getUserEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public UserDetailDTO updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // PATCH logic: update only non-null fields
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.email() != null) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmailIgnoreCase(request.email()) &&
                !user.getEmail().equalsIgnoreCase(request.email())) {
                throw new ValidationException("Email already exists");
            }
            user.setEmail(request.email());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }

        User updatedUser = userRepository.save(user);
        return toUserDetailDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // Clear seller references from Works if user is a seller
        if (user instanceof SellerUser) {
            workRepository.findAll().forEach(work -> {
                if (work.getSeller() != null && work.getSeller().getId().equals(id)) {
                    work.setSeller(null);
                    workRepository.save(work);
                }
            });
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void updatePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String uploadProfileImage(UUID userId, MultipartFile file) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

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
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    private UserSummaryDTO toUserSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.getUserType().name()
        );
    }

    private UserDetailDTO toUserDetailDTO(User user) {
        return new UserDetailDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getUserType().name()
        );
    }
}
