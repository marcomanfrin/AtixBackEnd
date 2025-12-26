package marcomanfrin.softwareops.DTO.workReports;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record AddWorkReportEntryRequest(
        @NotNull UUID taskId,
        @NotNull @Positive BigDecimal hours
) {}