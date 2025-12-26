package marcomanfrin.softwareops.DTO.works;

import java.time.LocalDate;
import java.util.UUID;

public record WorkSummaryResponse(
        UUID id,
        String name,
        String bidNumber,
        String orderNumber,
        LocalDate orderDate,
        boolean completed,
        boolean invoiced,
        Integer electricalSchemaProgression,
        Integer programmingProgression
) {
}
