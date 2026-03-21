package marcomanfrin.atixbackend.DTO.plants;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlantResponse(
        UUID id,
        String name,
        String notes,
        String nasDirectory,
        String pswPhrase,
        String pswPlatform,
        String pswStation,
        LocalDateTime createdAt
) {
}
