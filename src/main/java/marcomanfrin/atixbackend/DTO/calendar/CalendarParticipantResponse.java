package marcomanfrin.atixbackend.DTO.calendar;

import java.util.UUID;

public record CalendarParticipantResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String calendarColor
) {
}
