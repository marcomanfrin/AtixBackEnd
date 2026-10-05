package marcomanfrin.atixbackend.repositories;

import jakarta.persistence.LockModeType;
import marcomanfrin.atixbackend.entities.RapportinoCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RapportinoCounterRepository extends JpaRepository<RapportinoCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM RapportinoCounter c WHERE c.year = :year")
    Optional<RapportinoCounter> findForUpdate(@Param("year") Integer year);

    // Crea la riga dell'anno se manca, senza errori in caso di creazione concorrente
    @Modifying
    @Query(value = "INSERT INTO rapportino_counters (year, last_value) VALUES (:year, 0) ON CONFLICT (year) DO NOTHING", nativeQuery = true)
    void ensureYear(@Param("year") Integer year);
}
