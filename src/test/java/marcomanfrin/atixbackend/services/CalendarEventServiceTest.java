package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.calendar.CalendarEventRequest;
import marcomanfrin.atixbackend.DTO.calendar.CalendarEventResponse;
import marcomanfrin.atixbackend.entities.CalendarEvent;
import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.exceptions.ForbiddenException;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.exceptions.ValidationException;
import marcomanfrin.atixbackend.repositories.CalendarEventRepository;
import marcomanfrin.atixbackend.repositories.UserRepository;
import marcomanfrin.atixbackend.repositories.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class CalendarEventServiceTest {

    private CalendarEventRepository eventRepository;
    private UserRepository userRepository;
    private WorkRepository workRepository;
    private CalendarEventService service;

    private User anna;
    private User luca;
    private User admin;

    private static User user(String first, String last, UserRole role) {
        User user = new TechnicianUser();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setFirstName(first);
        user.setLastName(last);
        user.setRole(role);
        user.setCalendarColor("#E53935");
        return user;
    }

    private static CalendarEventRequest request(LocalDateTime start, LocalDateTime end, boolean allDay, List<UUID> participants) {
        return new CalendarEventRequest("Sopralluogo", null, null, start, end, allDay, participants, null);
    }

    @BeforeEach
    void setUp() {
        eventRepository = mock(CalendarEventRepository.class);
        userRepository = mock(UserRepository.class);
        workRepository = mock(WorkRepository.class);
        service = new CalendarEventService(eventRepository, userRepository, workRepository);

        anna = user("Anna", "Rossi", UserRole.USER);
        luca = user("Luca", "Bianchi", UserRole.USER);
        admin = user("Ada", "Admin", UserRole.ADMIN);

        for (User u : List.of(anna, luca, admin)) {
            when(userRepository.findById(u.getId())).thenReturn(Optional.of(u));
        }
        when(userRepository.findAllById(anyCollection())).thenAnswer(inv -> {
            Iterable<UUID> ids = inv.getArgument(0);
            List<User> all = List.of(anna, luca, admin);
            return java.util.stream.StreamSupport.stream(ids.spliterator(), false)
                    .flatMap(id -> all.stream().filter(u -> u.getId().equals(id)))
                    .toList();
        });
        when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CalendarEvent existingEventBy(User creator) {
        CalendarEvent event = new CalendarEvent();
        ReflectionTestUtils.setField(event, "id", UUID.randomUUID());
        event.setCreatedBy(creator);
        event.setTitle("Riunione");
        event.setStartAt(LocalDateTime.of(2026, 10, 12, 9, 0));
        event.setEndAt(LocalDateTime.of(2026, 10, 12, 10, 0));
        event.getParticipants().add(creator);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        return event;
    }

    @Test
    void createsTimedEventWithCallerAsCreatorOnly() {
        CalendarEventResponse response = service.createEvent(
                request(LocalDateTime.of(2026, 10, 12, 9, 0), LocalDateTime.of(2026, 10, 12, 11, 30), false, List.of(luca.getId())),
                anna);

        assertEquals(LocalDateTime.of(2026, 10, 12, 9, 0), response.startAt());
        assertEquals(LocalDateTime.of(2026, 10, 12, 11, 30), response.endAt());
        assertEquals(anna.getId(), response.createdBy().id());
        assertEquals(List.of(luca.getId()), response.participants().stream().map(p -> p.id()).toList());
        assertTrue(response.canEdit());
    }

    @Test
    void normalisesAllDayEventsToMidnightBoundaries() {
        CalendarEventResponse response = service.createEvent(
                request(LocalDateTime.of(2026, 10, 12, 15, 0), LocalDateTime.of(2026, 10, 14, 8, 0), true, List.of(anna.getId())),
                anna);

        assertEquals(LocalDateTime.of(2026, 10, 12, 0, 0), response.startAt());
        assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0), response.endAt());
        assertTrue(response.allDay());
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThrows(ValidationException.class, () -> service.createEvent(
                request(LocalDateTime.of(2026, 10, 12, 11, 0), LocalDateTime.of(2026, 10, 12, 9, 0), false, List.of(anna.getId())),
                anna));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void collapsesDuplicateParticipantsAndSortsByName() {
        CalendarEventResponse response = service.createEvent(
                request(LocalDateTime.of(2026, 10, 12, 9, 0), LocalDateTime.of(2026, 10, 12, 10, 0), false,
                        List.of(luca.getId(), anna.getId(), luca.getId(), admin.getId())),
                anna);

        assertEquals(List.of("Ada", "Anna", "Luca"), response.participants().stream().map(p -> p.firstName()).toList());
    }

    @Test
    void rejectsEmptyUnknownOrDeletedParticipants() {
        LocalDateTime s = LocalDateTime.of(2026, 10, 12, 9, 0);
        assertThrows(ValidationException.class, () -> service.createEvent(request(s, s.plusHours(1), false, List.of()), anna));
        assertThrows(ValidationException.class, () -> service.createEvent(request(s, s.plusHours(1), false, List.of(UUID.randomUUID())), anna));

        luca.setDeletedAt(LocalDateTime.now());
        assertThrows(ValidationException.class, () -> service.createEvent(request(s, s.plusHours(1), false, List.of(luca.getId())), anna));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownWork() {
        UUID workId = UUID.randomUUID();
        when(workRepository.findById(workId)).thenReturn(Optional.empty());
        LocalDateTime s = LocalDateTime.of(2026, 10, 12, 9, 0);
        CalendarEventRequest req = new CalendarEventRequest("T", null, null, s, s.plusHours(1), false, List.of(anna.getId()), workId);
        assertThrows(NotFoundException.class, () -> service.createEvent(req, anna));
    }

    @Test
    void participantWhoIsNotCreatorCannotEditOrDelete() {
        CalendarEvent event = existingEventBy(anna);
        event.getParticipants().add(luca);
        LocalDateTime s = LocalDateTime.of(2026, 10, 13, 9, 0);

        assertThrows(ForbiddenException.class, () -> service.updateEvent(event.getId(), request(s, s.plusHours(1), false, List.of(luca.getId())), luca));
        assertThrows(ForbiddenException.class, () -> service.deleteEvent(event.getId(), luca));
        verify(eventRepository, never()).delete(any(CalendarEvent.class));
        assertFalse(service.getEvent(event.getId(), luca).canEdit());
    }

    @Test
    void creatorAndAdminCanEditReplacingParticipants() {
        CalendarEvent event = existingEventBy(anna);
        LocalDateTime s = LocalDateTime.of(2026, 10, 13, 9, 0);

        CalendarEventResponse byCreator = service.updateEvent(event.getId(), request(s, s.plusHours(2), false, List.of(luca.getId())), anna);
        assertEquals(List.of(luca.getId()), byCreator.participants().stream().map(p -> p.id()).toList());
        assertEquals(s.plusHours(2), byCreator.endAt());

        CalendarEventResponse byAdmin = service.updateEvent(event.getId(), request(s, s.plusHours(1), false, List.of(anna.getId(), luca.getId())), admin);
        assertEquals(2, byAdmin.participants().size());
        assertTrue(byAdmin.canEdit());

        service.deleteEvent(event.getId(), admin);
        verify(eventRepository).delete(event);
    }

    @Test
    void listRequiresBoundedWindow() {
        LocalDateTime from = LocalDateTime.of(2026, 10, 1, 0, 0);
        assertThrows(ValidationException.class, () -> service.listEvents(null, from, false, null, anna));
        assertThrows(ValidationException.class, () -> service.listEvents(from, null, false, null, anna));
        assertThrows(ValidationException.class, () -> service.listEvents(from, from, false, null, anna));
        assertThrows(ValidationException.class, () -> service.listEvents(from, from.plusDays(101), false, null, anna));
        verify(eventRepository, never()).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class));
    }
}
