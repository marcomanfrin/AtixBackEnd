package marcomanfrin.atixbackend.DTO.plants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlantUpdateRequest(
        @Size(min = 2, max = 100, message = "Plant name must be between 2 and 100 characters")
        String name,

        String notes,

        String nasDirectory,

        String pswPhrase,

        String pswPlatform,

        String pswStation
) {
}
