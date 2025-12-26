package marcomanfrin.softwareops.DTO.workReports;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateWorkReportEntryRequest(
        @NotNull @Positive BigDecimal hours
) {}