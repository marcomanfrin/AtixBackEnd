package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, UUID>, JpaSpecificationExecutor<AccessLog> {
    Optional<AccessLog> findBySessionId(UUID sessionId);

    @Modifying
    @Query("UPDATE AccessLog a SET a.user = null WHERE a.user.id = :userId")
    void nullifyUserReferences(@Param("userId") UUID userId);
}
