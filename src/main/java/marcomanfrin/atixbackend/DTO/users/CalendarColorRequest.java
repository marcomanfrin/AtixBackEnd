package marcomanfrin.atixbackend.DTO.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CalendarColorRequest(
        @NotBlank(message = "Il colore è obbligatorio")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Il colore deve essere nel formato #RRGGBB")
        String calendarColor
) {
}
