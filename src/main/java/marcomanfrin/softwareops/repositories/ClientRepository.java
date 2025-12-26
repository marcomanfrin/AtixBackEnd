package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.Client;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<@NonNull Client, @NonNull UUID> {
    List<Client> findByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
