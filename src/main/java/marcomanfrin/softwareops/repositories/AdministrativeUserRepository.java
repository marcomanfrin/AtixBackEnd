package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.AdministrativeUser;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdministrativeUserRepository extends JpaRepository<@NonNull AdministrativeUser, @NonNull UUID> {
}
