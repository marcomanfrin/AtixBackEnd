package marcomanfrin.softwareops.controllers;

import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.login.LoginDTO;
import marcomanfrin.softwareops.DTO.login.LoginRespDTO;
import marcomanfrin.softwareops.DTO.registration.NewUserDTO;
import marcomanfrin.softwareops.DTO.registration.NewUserRespDTO;
import marcomanfrin.softwareops.exceptions.ValidationException;
import marcomanfrin.softwareops.services.AuthService;
import marcomanfrin.softwareops.services.UserService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginRespDTO login(@RequestBody LoginDTO body) {
        return new LoginRespDTO(this.authService.checkCredentialsAndGenerateToken(body));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public NewUserRespDTO createUser(@RequestBody @Valid NewUserDTO body, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            throw new ValidationException(validationResult.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
        }
        return this.userService.saveNewUser(body);
    }
}
