package marcomanfrin.atixbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.auth.LoginRequest;
import marcomanfrin.atixbackend.DTO.auth.LoginResponse;
import marcomanfrin.atixbackend.DTO.auth.LogoutRequest;
import marcomanfrin.atixbackend.services.AccessLogService;
import marcomanfrin.atixbackend.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final AccessLogService accessLogService;

    public AuthController(AuthService authService, AccessLogService accessLogService) {
        this.authService = authService;
        this.accessLogService = accessLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        if (request.sessionId() != null) {
            accessLogService.recordLogout(request.sessionId());
        }
        return ResponseEntity.noContent().build();
    }
}
