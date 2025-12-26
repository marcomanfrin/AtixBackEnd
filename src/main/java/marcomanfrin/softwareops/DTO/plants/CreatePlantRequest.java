package marcomanfrin.softwareops.DTO.plants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePlantRequest(@NotBlank @Size(min = 2, max = 80) String name,
                                 @Size(max = 500) String notes,
                                 @NotBlank @Size(min = 1, max = 50) String orderNumber,
                                 @NotNull UUID primaryClientId,
                                 UUID finalClientId) {
}
