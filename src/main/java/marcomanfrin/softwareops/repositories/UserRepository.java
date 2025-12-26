package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.TechnicianUser;
import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM TechnicianUser u")
    List<TechnicianUser> findAllTechnicians();

    Optional<User> findByEmail(String email);
}
