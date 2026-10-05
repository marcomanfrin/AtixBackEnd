package marcomanfrin.atixbackend.DTO.rapportini;

import jakarta.validation.constraints.Size;

import java.util.UUID;

// templateItemId presente: l'etichetta viene presa dal template; assente: voce libera con label obbligatoria
public record ChecklistAnswerRequest(
        UUID templateItemId,

        @Size(max = 255, message = "Label must be at most 255 characters")
        String label,

        boolean checked,

        @Size(max = 1000, message = "Note must be at most 1000 characters")
        String note
) {
}
