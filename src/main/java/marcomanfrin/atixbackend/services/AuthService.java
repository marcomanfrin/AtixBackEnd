package marcomanfrin.atixbackend.services;

import jakarta.servlet.http.HttpServletRequest;
import marcomanfrin.atixbackend.DTO.auth.LoginRequest;
import marcomanfrin.atixbackend.DTO.auth.LoginResponse;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.exceptions.UnauthorizedException;
import marcomanfrin.atixbackend.security.JWTTools;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UserService userService;
    private final JWTTools jwtTools;
    private final PasswordEncoder passwordEncoder;
    private final AccessLogService accessLogService;

    public AuthService(UserService userService, JWTTools jwtTools, PasswordEncoder passwordEncoder, AccessLogService accessLogService) {
        this.userService = userService;
        this.jwtTools = jwtTools;
        this.passwordEncoder = passwordEncoder;
        this.accessLogService = accessLogService;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = extractIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        Optional<User> userOpt = userService.getUserByEmail(request.email());
        if (userOpt.isEmpty()) {
            accessLogService.saveLog(null, request.email(), ip, userAgent, false, "USER_NOT_FOUND", null);
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            accessLogService.saveLog(user, request.email(), ip, userAgent, false, "INVALID_PASSWORD", null);
            throw new UnauthorizedException("Invalid credentials");
        }

        LocalDateTime jwtExpiry = jwtTools.getTokenExpiry();
        UUID sessionId = accessLogService.saveLog(user, request.email(), ip, userAgent, true, null, jwtExpiry);
        String token = jwtTools.createToken(user);
        return new LoginResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                sessionId != null ? sessionId.toString() : null
        );
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
