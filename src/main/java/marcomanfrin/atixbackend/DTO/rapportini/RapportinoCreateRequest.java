package marcomanfrin.atixbackend.DTO.rapportini;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record RapportinoCreateRequest(
        @NotNull(message = "Work ID is required")
        UUID workId,

        LocalDate interventionDate,  // Optional - defaults to today

        UUID technicianId,           // Optional - defaults to the current user

        UUID replacesId,             // Optional - VOID rapportino this one reissues

        @Pattern(regexp = "it|en", message = "Locale must be 'it' or 'en'")
        String locale
) {
}
