package marcomanfrin.atixbackend.DTO.works;

import jakarta.validation.constraints.NotNull;
import marcomanfrin.atixbackend.enums.WorkStatus;

public record ForceStatusRequest(
        @NotNull(message = "Status is required")
        WorkStatus status
) {}
