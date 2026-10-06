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
        UUID userId,
        String userName,
        UUID rapportinoId   // valorizzato per le righe generate da un rapportino firmato (sola lettura)
) {
}
