package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.auth.RegisterRequest;
import marcomanfrin.atixbackend.DTO.auth.UpdatePasswordRequest;
import marcomanfrin.atixbackend.DTO.users.CalendarColorRequest;
import marcomanfrin.atixbackend.DTO.users.UpdatedImageResp;
import marcomanfrin.atixbackend.DTO.users.UserDetailDTO;
import marcomanfrin.atixbackend.DTO.users.UserSummaryDTO;
import marcomanfrin.atixbackend.DTO.users.UserUpdateRequest;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.UserType;
import marcomanfrin.atixbackend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<UserDetailDTO> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserDetailDTO user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        List<UserSummaryDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<UserSummaryDTO>> getUsersByType(@PathVariable UserType type) {
        List<UserSummaryDTO> users = userService.getUsersByType(type);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    // TODO: @PreAuthorize("hasRole('OWNER') or @securityService.isSelf(#id, authentication)")
    public ResponseEntity<UserDetailDTO> getUserById(@PathVariable UUID id) {
        UserDetailDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or @securityService.isSelf(#id, authentication)")
    public ResponseEntity<UserDetailDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserDetailDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{id}/avatar", consumes = "multipart/form-data")
    @PreAuthorize("@securityService.isSelf(#id, authentication)")
    public ResponseEntity<UpdatedImageResp> uploadProfileImage(
            @PathVariable UUID id,
            @RequestParam("avatar") MultipartFile file
    ) {
        String imageUrl = userService.uploadProfileImage(id, file);
        return ResponseEntity.ok(new UpdatedImageResp(imageUrl));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("@securityService.isSelf(#id, authentication)")
    public ResponseEntity<Void> updatePassword(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/calendar-color")
    public ResponseEntity<UserDetailDTO> updateMyCalendarColor(
            @Valid @RequestBody CalendarColorRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateCalendarColor(user.getId(), request.calendarColor()));
    }

    @PatchMapping("/{id}/calendar-color")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or @securityService.isSelf(#id, authentication)")
    public ResponseEntity<UserDetailDTO> updateCalendarColor(
            @PathVariable UUID id,
            @Valid @RequestBody CalendarColorRequest request) {
        return ResponseEntity.ok(userService.updateCalendarColor(id, request.calendarColor()));
    }
}
