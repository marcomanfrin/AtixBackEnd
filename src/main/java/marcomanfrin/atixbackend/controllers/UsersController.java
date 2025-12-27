package marcomanfrin.atixbackend.controllers;

import marcomanfrin.atixbackend.DTO.users.UpdatedImageResp;
import marcomanfrin.atixbackend.DTO.users.UserDetailDTO;
import marcomanfrin.atixbackend.DTO.users.UserSummaryDTO;
import marcomanfrin.atixbackend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PatchMapping(path = "/{id}/avatar", consumes = "multipart/form-data")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<UpdatedImageResp> uploadProfileImage(
            @PathVariable UUID id,
            @RequestParam("avatar") MultipartFile file
    ) {
        String imageUrl = userService.uploadProfileImage(id, file);
        return ResponseEntity.ok(new UpdatedImageResp(imageUrl));
    }
}
