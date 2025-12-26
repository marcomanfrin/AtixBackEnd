package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.WorkReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkReportRepository extends JpaRepository<WorkReport, UUID> {
    java.util.Optional<WorkReport> findByWork_Id(UUID workId);
    Optional<WorkReport> findFirstByWork_IdOrderByCreatedAtDesc(UUID workId);
}
