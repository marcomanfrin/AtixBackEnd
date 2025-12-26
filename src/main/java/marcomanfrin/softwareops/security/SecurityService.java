package marcomanfrin.softwareops.security;

import marcomanfrin.softwareops.entities.AdministrativeUser;
import marcomanfrin.softwareops.entities.TechnicianUser;
import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("securityService")
public class SecurityService {

    private final UserRepository userRepository;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isSelf(UUID id, Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return principal.getId().equals(id);
    }

    public boolean isAdministrative(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return principal instanceof AdministrativeUser;
    }

    public boolean isTechnician(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return principal instanceof TechnicianUser;
    }
}