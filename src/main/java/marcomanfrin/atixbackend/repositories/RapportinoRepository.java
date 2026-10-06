package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.Rapportino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RapportinoRepository extends JpaRepository<Rapportino, UUID>, JpaSpecificationExecutor<Rapportino> {

    // Lista: carica commessa e tecnico in un'unica query (niente N+1)
    @Override
    @EntityGraph(attributePaths = {"work", "technician"})
    Page<Rapportino> findAll(Specification<Rapportino> spec, Pageable pageable);

    // In attesa di firma ma senza alcuna richiesta ancora utilizzabile (scaduta, revocata o usata)
    @Query("""
            SELECT r FROM Rapportino r
            WHERE r.status = marcomanfrin.atixbackend.enums.RapportinoStatus.AWAITING_SIGNATURE
              AND NOT EXISTS (
                SELECT q FROM RapportinoSignatureRequest q
                WHERE q.rapportino = r AND q.usedAt IS NULL AND q.revokedAt IS NULL AND q.expiresAt > :now)
            """)
    List<Rapportino> findAwaitingWithoutUsableRequest(@Param("now") LocalDateTime now);
}
