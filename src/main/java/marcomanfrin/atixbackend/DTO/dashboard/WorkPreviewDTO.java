package marcomanfrin.atixbackend.DTO.dashboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkPreviewDTO(
        UUID id,
        String name,
        boolean completed,
        LocalDateTime createdAt
) {
}
