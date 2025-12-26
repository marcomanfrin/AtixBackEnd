package marcomanfrin.softwareops.DTO.plants;

import java.util.UUID;

public record PlantResponse(
        UUID id,
        String name,
        String notes,
        String nasDirectory,
        String pswPhrase,
        String pswPlatform,
        String pswStation
) {
}
