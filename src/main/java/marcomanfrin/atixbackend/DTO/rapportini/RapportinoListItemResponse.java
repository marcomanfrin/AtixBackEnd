package marcomanfrin.atixbackend.DTO.rapportini;

import marcomanfrin.atixbackend.enums.RapportinoStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// Elemento di lista: mai la firma, mai URL di storage
public record RapportinoListItemResponse(
        UUID id,
        String number,
        RapportinoStatus status,
        LocalDate interventionDate,
        UUID workId,
        String workName,
        String orderNumber,
        String clientName,
        String plantLabel,
        UUID technicianId,
        String technicianName,
        BigDecimal totalHours,
        LocalDateTime signedAt,
        boolean pdfAvailable
) {
}
