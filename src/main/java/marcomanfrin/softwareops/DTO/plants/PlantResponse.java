package marcomanfrin.softwareops.DTO.plants;

import java.util.UUID;

public record PlantResponse(
        UUID id,
        String name,
        String notes,
        String orderNumber,
        UUID primaryClientId,
        UUID finalClientId,
        boolean invoiced
){}
