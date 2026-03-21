package marcomanfrin.atixbackend.DTO.workReports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WorkReportEntryResponse(
        UUID id,
        UUID reportId,
        String description,
        BigDecimal hours,
        LocalDate date,
        UUID technicianId,
        String technicianName
) {
}
