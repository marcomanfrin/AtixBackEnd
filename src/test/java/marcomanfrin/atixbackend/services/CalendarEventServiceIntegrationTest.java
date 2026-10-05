package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.calendar.CalendarEventRequest;
import marcomanfrin.atixbackend.DTO.calendar.CalendarEventResponse;
import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CalendarEventServiceIntegrationTest {

    // Finestra lontana nel futuro per non interferire con dati reali
    private static final LocalDateTime OCT_FROM = LocalDateTime.of(2096, 10, 1, 0, 0);
    private static final LocalDateTime OCT_TO = LocalDateTime.of(2096, 11, 1, 0, 0);

    @Autowired
    private CalendarEventService service;

    @Autowired
    private UserRepository userRepository;

    private User anna;
    private User luca;
    private User marco;

    private User newUser(String firstName) {
        User user = new TechnicianUser();
        user.setFirstName(firstName);
        user.setLastName("CalTest");
        user.setEmail(firstName.toLowerCase() + "-" + UUID.randomUUID() + "@caltest.local");
        user.setPasswordHash("x");
        user.setRole(UserRole.USER);
        user.setCalendarColor("#1E88E5");
        return userRepository.save(user);
    }

    private CalendarEventResponse create(User caller, LocalDateTime start, LocalDateTime end, User... participants) {
        return service.createEvent(new CalendarEventRequest(
                "Evento", null, null, start, end, false,
                java.util.Arrays.stream(participants).map(User::getId).toList(), null), caller);
    }

    private List<UUID> ids(List<CalendarEventResponse> events) {
        return events.stream().map(CalendarEventResponse::id).toList();
    }

    @BeforeEach
    void setUp() {
        anna = newUser("Anna");
        luca = newUser("Luca");
        marco = newUser("Marco");
    }

    @Test
    void overlapIncludesSpanningEventsAndExcludesTouchingOnes() {
        CalendarEventResponse spanning = create(anna, LocalDateTime.of(2096, 9, 29, 9, 0), LocalDateTime.of(2096, 10, 2, 9, 0), anna);
        CalendarEventResponse touching = create(anna, LocalDateTime.of(2096, 9, 30, 9, 0), OCT_FROM, anna);
        CalendarEventResponse inside = create(anna, LocalDateTime.of(2096, 10, 15, 9, 0), LocalDateTime.of(2096, 10, 15, 10, 0), anna);
        CalendarEventResponse startsAtEnd = create(anna, OCT_TO, OCT_TO.plusHours(1), anna);

        List<UUID> result = ids(service.listEvents(OCT_FROM, OCT_TO, false, List.of(anna.getId()), anna));

        assertEquals(List.of(spanning.id(), inside.id()), result);
        assertFalse(result.contains(touching.id()));
        assertFalse(result.contains(startsAtEnd.id()));
    }

    @Test
    void mineReturnsOnlyEventsWhereCallerParticipates() {
        CalendarEventResponse forMe = create(luca, OCT_FROM.plusDays(1), OCT_FROM.plusDays(1).plusHours(1), anna);
        CalendarEventResponse group = create(anna, OCT_FROM.plusDays(2), OCT_FROM.plusDays(2).plusHours(1), anna, luca, marco);
        CalendarEventResponse forOthers = create(anna, OCT_FROM.plusDays(3), OCT_FROM.plusDays(3).plusHours(1), luca);

        List<UUID> mine = ids(service.listEvents(OCT_FROM, OCT_TO, true, null, anna));
        assertEquals(List.of(forMe.id(), group.id()), mine);
        assertFalse(mine.contains(forOthers.id()));

        // visibilità condivisa: senza filtri Marco vede anche eventi in cui non è coinvolto
        List<UUID> all = ids(service.listEvents(OCT_FROM, OCT_TO, false, List.of(anna.getId(), luca.getId(), marco.getId()), marco));
        assertTrue(all.containsAll(List.of(forMe.id(), group.id(), forOthers.id())));
    }

    @Test
    void participantFilterReturnsEachEventOnceWithAllParticipants() {
        CalendarEventResponse group = create(anna, OCT_FROM.plusDays(2), OCT_FROM.plusDays(2).plusHours(1), anna, luca, marco);
        create(anna, OCT_FROM.plusDays(3), OCT_FROM.plusDays(3).plusHours(1), marco);

        List<CalendarEventResponse> result = service.listEvents(OCT_FROM, OCT_TO, false, List.of(anna.getId(), luca.getId()), marco);

        assertEquals(1, result.size());
        assertEquals(group.id(), result.get(0).id());
        assertEquals(3, result.get(0).participants().size());
        assertFalse(result.get(0).canEdit());
    }

    @Test
    void mineAndParticipantFilterCombine() {
        create(anna, OCT_FROM.plusDays(2), OCT_FROM.plusDays(2).plusHours(1), anna, luca);
        CalendarEventResponse annaMarco = create(anna, OCT_FROM.plusDays(3), OCT_FROM.plusDays(3).plusHours(1), anna, marco);

        List<UUID> result = ids(service.listEvents(OCT_FROM, OCT_TO, true, List.of(marco.getId()), anna));
        assertEquals(List.of(annaMarco.id()), result);
    }
}
