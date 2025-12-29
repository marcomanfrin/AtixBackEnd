package marcomanfrin.atixbackend.DTO.works;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkAssignmentResponse(
        UUID id,
        UUID technicianId,
        String technicianFirstName,
        String technicianLastName,
        String technicianEmail,
        LocalDateTime assignedAt
) {
}
