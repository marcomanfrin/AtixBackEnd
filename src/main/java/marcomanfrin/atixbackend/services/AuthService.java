package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.auth.LoginRequest;
import marcomanfrin.atixbackend.DTO.auth.LoginResponse;
import marcomanfrin.atixbackend.DTO.auth.RegisterRequest;
import marcomanfrin.atixbackend.DTO.auth.UpdatePasswordRequest;
import marcomanfrin.atixbackend.DTO.users.UserDetailDTO;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.exceptions.UnauthorizedException;
import marcomanfrin.atixbackend.security.JWTTools;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final UserService userService;
    private final JWTTools jwtTools;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JWTTools jwtTools, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTools = jwtTools;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetailDTO register(RegisterRequest request) {
        return userService.registerUser(request);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService
                .getUserByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtTools.createToken(user);

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    public void updatePassword(UUID userId, UpdatePasswordRequest request) {
        userService.updatePassword(userId, request.currentPassword(), request.newPassword());
    }
}
