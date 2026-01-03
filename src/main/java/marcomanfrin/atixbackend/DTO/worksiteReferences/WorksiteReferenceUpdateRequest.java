package marcomanfrin.atixbackend.DTO.worksiteReferences;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorksiteReferenceUpdateRequest(
        @Size(min = 2, max = 100, message = "Worksite reference name must be between 2 and 100 characters")
        String name,
        String telephone
) {
}
