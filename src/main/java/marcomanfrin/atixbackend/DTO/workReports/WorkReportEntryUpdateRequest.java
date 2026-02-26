package marcomanfrin.atixbackend.DTO.workReports;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WorkReportEntryUpdateRequest(
        UUID workId,

        String description,

        // F8: allow hours = 0 (inclusive = true)
        @DecimalMin(value = "0.0", inclusive = true, message = "Hours must be 0 or greater")
        BigDecimal hours,

        LocalDate date
) {
}
