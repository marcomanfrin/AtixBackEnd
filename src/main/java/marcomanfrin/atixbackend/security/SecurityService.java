package marcomanfrin.atixbackend.security;

import marcomanfrin.atixbackend.entities.users.AdministrativeUser;
import marcomanfrin.atixbackend.entities.users.SellerUser;
import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.repositories.UserRepository;
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

    public boolean isSeller(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return principal instanceof SellerUser;
    }
}