package marcomanfrin.softwareops.controllers;

import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.auth.LoginRequest;
import marcomanfrin.softwareops.DTO.auth.LoginResponse;
import marcomanfrin.softwareops.DTO.auth.RegisterRequest;
import marcomanfrin.softwareops.DTO.auth.UpdatePasswordRequest;
import marcomanfrin.softwareops.DTO.users.UserDetailDTO;
import marcomanfrin.softwareops.entities.users.User;
import marcomanfrin.softwareops.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDetailDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDetailDTO user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(user.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
