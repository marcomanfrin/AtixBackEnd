package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.WorkReportEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkReportEntryRepository extends JpaRepository<WorkReportEntry, UUID> {
    List<WorkReportEntry> findByReport_Id(UUID reportId);

    @Query("""
        SELECT COALESCE(SUM(e.hours), 0)
        FROM WorkReportEntry e
        WHERE e.report.id = :reportId
    """)
    BigDecimal sumHoursByReportId(@Param("reportId") UUID reportId);
}
