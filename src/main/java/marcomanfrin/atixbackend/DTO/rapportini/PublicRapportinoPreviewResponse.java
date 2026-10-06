package marcomanfrin.atixbackend.DTO.rapportini;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Anteprima per la firma remota: nessun identificativo interno, nessun dato di altri rapportini/commesse
public record PublicRapportinoPreviewResponse(
        String number,
        LocalDate interventionDate,
        String locale,
        String clientName,
        String plantLabel,
        String description,
        List<Item> checklist,
        List<Material> materials,
        BigDecimal workHours,
        BigDecimal travelHours,
        BigDecimal totalHours,
        LocalDateTime expiresAt
) {
    public record Item(String label, boolean checked, String note) {}

    public record Material(String description, BigDecimal quantity) {}
}
