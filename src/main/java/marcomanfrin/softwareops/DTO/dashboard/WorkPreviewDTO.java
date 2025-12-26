package marcomanfrin.softwareops.DTO.dashboard;

import marcomanfrin.softwareops.enums.WorkStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkPreviewDTO(
        UUID id,
        WorkStatus status,
        LocalDateTime createdAt
) {
}
