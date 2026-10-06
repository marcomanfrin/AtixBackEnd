package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.calendar.CalendarEventRequest;
import marcomanfrin.atixbackend.DTO.calendar.CalendarEventResponse;
import marcomanfrin.atixbackend.entities.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ICalendarEventService {
    List<CalendarEventResponse> listEvents(LocalDateTime from, LocalDateTime to, boolean mine, List<UUID> participantIds, User caller);
    CalendarEventResponse getEvent(UUID id, User caller);
    CalendarEventResponse createEvent(CalendarEventRequest request, User caller);
    CalendarEventResponse updateEvent(UUID id, CalendarEventRequest request, User caller);
    void deleteEvent(UUID id, User caller);
}
