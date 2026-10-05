package marcomanfrin.atixbackend.DTO.calendar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CalendarEventResponse(
        UUID id,
        String title,
        String description,
        String location,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean allDay,
        List<CalendarParticipantResponse> participants,
        CalendarParticipantResponse createdBy,
        CalendarWorkRefResponse work,
        boolean canEdit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
