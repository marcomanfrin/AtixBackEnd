package marcomanfrin.softwareops.DTO.plants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlantRequest(
        @NotBlank(message = "Plant name is required")
        @Size(min = 2, max = 100, message = "Plant name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Notes are required")
        String notes,

        @NotBlank(message = "NAS directory is required")
        String nasDirectory,

        @NotBlank(message = "Password phrase is required")
        String pswPhrase,

        @NotBlank(message = "Password platform is required")
        String pswPlatform,

        @NotBlank(message = "Password station is required")
        String pswStation
) {
}
