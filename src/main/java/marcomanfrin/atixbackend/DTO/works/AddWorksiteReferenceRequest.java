package marcomanfrin.atixbackend.DTO.works;

import jakarta.validation.constraints.NotNull;
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;

import java.util.UUID;

public record AddWorksiteReferenceRequest(
        @NotNull(message = "Worksite reference ID is required")
        UUID worksiteReferenceId,

        @NotNull(message = "Role is required")
        WorksiteReferenceRole role
) {
}
