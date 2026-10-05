package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.RapportinoSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RapportinoSignatureRepository extends JpaRepository<RapportinoSignature, UUID> {
    Optional<RapportinoSignature> findByRapportinoId(UUID rapportinoId);

    boolean existsByRapportinoId(UUID rapportinoId);
}
