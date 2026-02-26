package marcomanfrin.atixbackend.DTO.workReports;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WorkReportEntryRequest(
        @NotNull(message = "Work ID is required")
        UUID workId,

        @NotBlank(message = "Description is required")
        String description,

        // F8: allow hours = 0 (inclusive = true)
        @NotNull(message = "Hours are required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Hours must be 0 or greater")
        BigDecimal hours,

        LocalDate date  // Optional - defaults to today if not provided
) {
}
