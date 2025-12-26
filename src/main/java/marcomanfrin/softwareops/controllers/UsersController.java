package marcomanfrin.softwareops.controllers;

import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.users.ChangePasswordRequest;
import marcomanfrin.softwareops.DTO.users.UpdateUserRequest;
import marcomanfrin.softwareops.DTO.users.UpdateUserRoleRequest;
import marcomanfrin.softwareops.DTO.users.UserResponseDTO;
import marcomanfrin.softwareops.DTO.users.UpdatedImageResp;
import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.enums.UserRole;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.services.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final IUserService usersService;

    public UsersController(IUserService usersService) {
        this.usersService = usersService;
    }

    @GetMapping
    public Page<UserResponseDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 100) ? 20 : size;
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("username").ascending());
        return usersService.getAllUsers(pageRequest)
                .map(UserResponseDTO::fromEntity);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or @securityService.isSelf(#id, authentication)")
    public UserResponseDTO getUserById(@PathVariable UUID id) {
        return UserResponseDTO.fromEntity(usersService.getUserById(id).orElseThrow());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserResponseDTO updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User updated = usersService.updateUser(id, request);
        return UserResponseDTO.fromEntity(updated);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        User updated = usersService.changeUserRole(id, request.role());
        return UserResponseDTO.fromEntity(updated);
    }

    @PostMapping("/{id}/password")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest payload
    ) {
        usersService.changePassword(id, payload);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        usersService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/technicians")
    public List<UserResponseDTO> getTechnicians() {
        return usersService.getTechnicians()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/role/{role}")
    public List<UserResponseDTO> getUsersByRole(@PathVariable UserRole role) {
        return usersService.getUsersByRole(role)
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @PatchMapping(path = "/{id}/avatar", consumes = "multipart/form-data")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<UpdatedImageResp> uploadProfileImage(
            @PathVariable UUID id,
            @RequestParam("avatar") MultipartFile file
    ) {
        String imageUrl = usersService.uploadProfileImage(id, file);
        return ResponseEntity.ok(new UpdatedImageResp(imageUrl));
    }
}
