package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.TechnicianUser;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TechnicianUserRepository extends JpaRepository<@NonNull TechnicianUser, @NonNull UUID> {
}
