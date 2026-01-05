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

        @NotNull(message = "Hours are required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Hours must be greater than 0")
        BigDecimal hours,

        LocalDate date  // Optional - defaults to today if not provided
) {
}
