package marcomanfrin.atixbackend.DTO.rapportini;

import java.util.UUID;

public record ChecklistTemplateItemResponse(
        UUID id,
        String code,
        String labelIt,
        String labelEn,
        int position
) {
}
