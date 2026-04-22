package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    List<User> findByDeletedAtIsNull();

    List<User> findByRoleAndDeletedAtIsNull(UserRole role);

    @Query("SELECT u FROM TechnicianUser u WHERE u.deletedAt IS NULL")
    List<TechnicianUser> findAllActiveTechnicians();

    Optional<User> findByEmail(String email);
}
