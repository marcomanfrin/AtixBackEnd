package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.Work;
import marcomanfrin.atixbackend.entities.WorkReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkReportRepository extends JpaRepository<WorkReport, UUID> {
    Optional<WorkReport> findByWork(Work work);

    @Query("SELECT wr FROM WorkReport wr WHERE wr.work.id = :workId")
    Optional<WorkReport> findByWorkId(@Param("workId") UUID workId);

    boolean existsByWork(Work work);
}
