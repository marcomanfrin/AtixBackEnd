package marcomanfrin.atixbackend.DTO.rapportini;

import java.util.UUID;

public record ChecklistAnswerResponse(
        UUID id,
        UUID templateItemId,
        String label,
        boolean checked,
        String note,
        int position
) {
}
