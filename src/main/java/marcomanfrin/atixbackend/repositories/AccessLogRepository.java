package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, UUID>, JpaSpecificationExecutor<AccessLog> {
    Optional<AccessLog> findBySessionId(UUID sessionId);
}
