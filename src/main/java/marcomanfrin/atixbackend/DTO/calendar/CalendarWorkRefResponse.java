package marcomanfrin.atixbackend.DTO.calendar;

import java.util.UUID;

public record CalendarWorkRefResponse(
        UUID id,
        String label
) {
}
