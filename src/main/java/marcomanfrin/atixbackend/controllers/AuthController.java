package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.auth.LoginRequest;
import marcomanfrin.atixbackend.DTO.auth.LoginResponse;
import marcomanfrin.atixbackend.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
