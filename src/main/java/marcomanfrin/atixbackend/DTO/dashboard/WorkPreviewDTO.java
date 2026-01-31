package marcomanfrin.atixbackend.DTO.dashboard;

import marcomanfrin.atixbackend.enums.WorkStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkPreviewDTO(
        UUID id,
        String name,
        WorkStatus status,
        LocalDateTime createdAt
) {
}
