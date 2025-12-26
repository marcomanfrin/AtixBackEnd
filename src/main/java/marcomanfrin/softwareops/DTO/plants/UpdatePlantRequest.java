package marcomanfrin.softwareops.DTO.plants;

import jakarta.validation.constraints.Size;

public record UpdatePlantRequest(
        @Size(min = 2, max = 80) String name,
        @Size(max = 500) String notes,
        @Size(min = 1, max = 50) String orderNumber
) {}
