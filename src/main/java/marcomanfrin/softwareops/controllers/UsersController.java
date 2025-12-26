package marcomanfrin.softwareops.controllers;

import marcomanfrin.softwareops.DTO.users.UserDetailDTO;
import marcomanfrin.softwareops.DTO.users.UserSummaryDTO;
import marcomanfrin.softwareops.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        List<UserSummaryDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or #id == authentication.principal.id")
    public ResponseEntity<UserDetailDTO> getUserById(@PathVariable UUID id) {
        UserDetailDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
