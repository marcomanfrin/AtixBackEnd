package marcomanfrin.softwareops.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import marcomanfrin.softwareops.DTO.login.LoginDTO;
import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.exceptions.UnauthorizedException;
import marcomanfrin.softwareops.security.JWTTools;

@Service
public class AuthService {
	private final UserService userService;
	private final JWTTools jwtTools;
	private final PasswordEncoder bcrypt;

    public AuthService(UserService userService, JWTTools jwtTools, PasswordEncoder bcrypt) {
        this.userService = userService;
        this.jwtTools = jwtTools;
        this.bcrypt = bcrypt;
    }

    public String checkCredentialsAndGenerateToken(LoginDTO body) {
        User user = userService
                .getUserByUsername(body.userName())
                .orElseThrow(() -> new UnauthorizedException("Utente non trovato"));


        if (bcrypt.matches(body.password(), user.getPassword())) {
			return jwtTools.createToken(user);
		}
        else {
			throw new UnauthorizedException("Credenziali errate");
		}
	}
}
