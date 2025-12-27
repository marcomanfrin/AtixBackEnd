package marcomanfrin.atixbackend.DTO.workReports;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkReportEntryUpdateRequest(
        UUID workId,

        String description,

        @DecimalMin(value = "0.0", inclusive = false, message = "Hours must be greater than 0")
        BigDecimal hours
) {
}
