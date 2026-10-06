package marcomanfrin.atixbackend.DTO.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Per gli eventi allDay la data di endAt indica l'ultimo giorno incluso
 * (l'orario viene ignorato): il server normalizza a [inizio primo giorno, inizio giorno successivo all'ultimo).
 */
public record CalendarEventRequest(
        @NotBlank(message = "Il titolo è obbligatorio")
        @Size(max = 200, message = "Il titolo non può superare i 200 caratteri")
        String title,

        String description,

        @Size(max = 255, message = "Il luogo non può superare i 255 caratteri")
        String location,

        @NotNull(message = "La data di inizio è obbligatoria")
        LocalDateTime startAt,

        @NotNull(message = "La data di fine è obbligatoria")
        LocalDateTime endAt,

        boolean allDay,

        @NotEmpty(message = "Serve almeno un partecipante")
        List<UUID> participantIds,

        UUID workId
) {
}
