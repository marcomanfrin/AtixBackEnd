package marcomanfrin.atixbackend.DTO.worksiteReferences;

import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;

import java.util.UUID;

public record WorksiteReferenceAssignmentResponse(
        UUID id,
        UUID worksiteReferenceId,
        String worksiteReferenceName,
        WorksiteReferenceRole role
) {
}
