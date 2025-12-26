package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.users.TechnicianUser;
import marcomanfrin.softwareops.entities.users.User;
import marcomanfrin.softwareops.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM TechnicianUser u")
    List<TechnicianUser> findAllTechnicians();

    Optional<User> findByEmail(String email);
}
