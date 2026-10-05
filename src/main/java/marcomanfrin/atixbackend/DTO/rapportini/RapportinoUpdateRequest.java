package marcomanfrin.atixbackend.DTO.rapportini;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import marcomanfrin.atixbackend.enums.RapportinoWorkState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// PATCH: si aggiornano solo i campi non null; le liste, se presenti, sostituiscono quelle esistenti
public record RapportinoUpdateRequest(
        LocalDate interventionDate,
        UUID technicianId,

        UUID clientId,
        UUID plantId,
        UUID worksiteReferenceId,

        Boolean typeMaintenance,
        Boolean typeCallOut,
        Boolean typeQuote,
        Boolean typeWarranty,

        @Size(max = 4000, message = "Description must be at most 4000 characters")
        String description,

        @DecimalMin(value = "0.0", message = "Work hours must be 0 or greater")
        BigDecimal workHours,

        @DecimalMin(value = "0.0", message = "Travel hours must be 0 or greater")
        BigDecimal travelHours,

        @DecimalMin(value = "0.0", message = "Travel km must be 0 or greater")
        BigDecimal travelKm,

        Boolean meal,
        Boolean parking,
        RapportinoWorkState workState,

        @Pattern(regexp = "it|en", message = "Locale must be 'it' or 'en'")
        String locale,

        @Valid
        List<ChecklistAnswerRequest> checklistAnswers,

        @Valid
        List<MaterialRequest> materials
) {
}
