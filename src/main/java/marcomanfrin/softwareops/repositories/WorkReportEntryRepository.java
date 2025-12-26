package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.WorkReport;
import marcomanfrin.softwareops.entities.WorkReportEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkReportEntryRepository extends JpaRepository<WorkReportEntry, UUID> {
    List<WorkReportEntry> findByReport(WorkReport report);

    @Query("SELECT wre FROM WorkReportEntry wre WHERE wre.report.id = :reportId")
    List<WorkReportEntry> findByReportId(@Param("reportId") UUID reportId);
}
