package marcomanfrin.atixbackend.DTO.works;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddWorksiteReferenceRequest(
        @NotNull(message = "Worksite reference ID is required")
        UUID worksiteReferenceId
) {
}
