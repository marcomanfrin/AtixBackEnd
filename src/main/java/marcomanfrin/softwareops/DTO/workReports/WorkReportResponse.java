package marcomanfrin.softwareops.DTO.workReports;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkReportResponse(
        UUID id,
        UUID workId,
        BigDecimal totalHours,
        LocalDateTime createdAt,
        List<WorkReportEntryResponse> entries
) {
}
