package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.Client;
import marcomanfrin.atixbackend.enums.ClientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findByType(ClientType type);
    List<Client> findByNameContainingIgnoreCase(String name);
}
