package marcomanfrin.atixbackend.DTO.rapportini;

import java.math.BigDecimal;
import java.util.UUID;

public record MaterialResponse(
        UUID id,
        String description,
        BigDecimal quantity,
        int position
) {
}
