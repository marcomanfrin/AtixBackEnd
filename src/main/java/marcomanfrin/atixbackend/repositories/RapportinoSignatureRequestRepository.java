package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.RapportinoSignatureRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RapportinoSignatureRequestRepository extends JpaRepository<RapportinoSignatureRequest, UUID> {
    Optional<RapportinoSignatureRequest> findByTokenHash(String tokenHash);

    // Lock sulla richiesta: due invii concorrenti con lo stesso token vengono serializzati
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM RapportinoSignatureRequest q WHERE q.tokenHash = :tokenHash")
    Optional<RapportinoSignatureRequest> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<RapportinoSignatureRequest> findByRapportinoIdAndUsedAtIsNullAndRevokedAtIsNull(UUID rapportinoId);

    List<RapportinoSignatureRequest> findByRapportinoId(UUID rapportinoId);
}
