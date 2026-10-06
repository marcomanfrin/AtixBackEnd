package marcomanfrin.atixbackend.DTO.rapportini;

import marcomanfrin.atixbackend.enums.RapportinoStatus;
import marcomanfrin.atixbackend.enums.RapportinoWorkState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RapportinoDetailResponse(
        UUID id,
        String number,
        RapportinoStatus status,
        LocalDate interventionDate,
        String locale,

        UUID workId,
        String workName,

        UUID technicianId,
        String technicianName,

        UUID clientId,
        UUID plantId,
        UUID worksiteReferenceId,
        String clientName,
        String clientReference,
        String plantLabel,
        String orderNumber,

        boolean typeMaintenance,
        boolean typeCallOut,
        boolean typeQuote,
        boolean typeWarranty,
        String description,

        BigDecimal workHours,
        BigDecimal travelHours,
        BigDecimal totalHours,
        BigDecimal travelKm,
        boolean meal,
        boolean parking,
        RapportinoWorkState workState,

        List<ChecklistAnswerResponse> checklistAnswers,
        List<MaterialResponse> materials,

        SignatureResponse signature,        // null finche' non firmato
        boolean pdfAvailable,
        String pdfHash,

        LocalDateTime signatureRequestExpiresAt, // valorizzato solo se AWAITING_SIGNATURE

        LocalDateTime voidedAt,
        String voidedByName,
        UUID replacesId,
        String replacesNumber,

        UUID createdById,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
