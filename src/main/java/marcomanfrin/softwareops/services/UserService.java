package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.auth.RegisterRequest;
import marcomanfrin.softwareops.DTO.users.UserDetailDTO;
import marcomanfrin.softwareops.DTO.users.UserSummaryDTO;
import marcomanfrin.softwareops.ServiceInterfaces.IUserService;
import marcomanfrin.softwareops.entities.users.AdministrativeUser;
import marcomanfrin.softwareops.entities.users.SellerUser;
import marcomanfrin.softwareops.entities.users.TechnicianUser;
import marcomanfrin.softwareops.entities.users.User;
import marcomanfrin.softwareops.enums.UserRole;
import marcomanfrin.softwareops.enums.UserType;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.exceptions.UnauthorizedException;
import marcomanfrin.softwareops.exceptions.ValidationException;
import marcomanfrin.softwareops.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        return toUserDetailDTO(savedUser);
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
    public void updateProfileImage(UUID userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
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
