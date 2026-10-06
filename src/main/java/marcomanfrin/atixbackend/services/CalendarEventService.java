package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.calendar.CalendarEventRequest;
import marcomanfrin.atixbackend.DTO.calendar.CalendarEventResponse;
import marcomanfrin.atixbackend.DTO.calendar.CalendarParticipantResponse;
import marcomanfrin.atixbackend.DTO.calendar.CalendarWorkRefResponse;
import marcomanfrin.atixbackend.ServiceInterfaces.ICalendarEventService;
import marcomanfrin.atixbackend.entities.CalendarEvent;
import marcomanfrin.atixbackend.entities.Work;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.exceptions.ForbiddenException;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.exceptions.ValidationException;
import marcomanfrin.atixbackend.repositories.CalendarEventRepository;
import marcomanfrin.atixbackend.repositories.UserRepository;
import marcomanfrin.atixbackend.repositories.WorkRepository;
import marcomanfrin.atixbackend.specifications.CalendarEventSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CalendarEventService implements ICalendarEventService {

    public static final long MAX_WINDOW_DAYS = 100;

    private static final Comparator<User> BY_NAME = Comparator
            .comparing((User u) -> u.getFirstName() == null ? "" : u.getFirstName(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(u -> u.getLastName() == null ? "" : u.getLastName(), String.CASE_INSENSITIVE_ORDER);

    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;
    private final WorkRepository workRepository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository,
                                UserRepository userRepository,
                                WorkRepository workRepository) {
        this.calendarEventRepository = calendarEventRepository;
        this.userRepository = userRepository;
        this.workRepository = workRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> listEvents(LocalDateTime from, LocalDateTime to, boolean mine,
                                                  List<UUID> participantIds, User caller) {
        if (from == null || to == null) {
            throw new ValidationException("I parametri 'from' e 'to' sono obbligatori");
        }
        if (!to.isAfter(from)) {
            throw new ValidationException("'to' deve essere successivo a 'from'");
        }
        if (Duration.between(from, to).compareTo(Duration.ofDays(MAX_WINDOW_DAYS)) > 0) {
            throw new ValidationException("La finestra richiesta non può superare " + MAX_WINDOW_DAYS + " giorni");
        }

        Specification<CalendarEvent> spec = CalendarEventSpecification.overlaps(from, to)
                .and(CalendarEventSpecification.participantIn(participantIds));
        if (mine) {
            spec = spec.and(CalendarEventSpecification.hasParticipant(caller.getId()));
        }

        return calendarEventRepository.findAll(spec, Sort.by("startAt", "endAt")).stream()
                .map(event -> toResponse(event, caller))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarEventResponse getEvent(UUID id, User caller) {
        return toResponse(findEvent(id), caller);
    }

    @Override
    @Transactional
    public CalendarEventResponse createEvent(CalendarEventRequest request, User caller) {
        User creator = userRepository.findById(caller.getId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + caller.getId()));

        CalendarEvent event = new CalendarEvent();
        event.setCreatedBy(creator);
        apply(event, request);

        return toResponse(calendarEventRepository.save(event), caller);
    }

    @Override
    @Transactional
    public CalendarEventResponse updateEvent(UUID id, CalendarEventRequest request, User caller) {
        CalendarEvent event = findEvent(id);
        checkCanEdit(event, caller);
        apply(event, request);
        return toResponse(calendarEventRepository.save(event), caller);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID id, User caller) {
        CalendarEvent event = findEvent(id);
        checkCanEdit(event, caller);
        calendarEventRepository.delete(event);
    }

    public static boolean canEdit(CalendarEvent event, User caller) {
        return caller.getRole() == UserRole.ADMIN
                || caller.getRole() == UserRole.OWNER
                || event.getCreatedBy().getId().equals(caller.getId());
    }

    /**
     * Normalizza l'intervallo: per gli eventi "tutto il giorno" restituisce
     * [mezzanotte del primo giorno, mezzanotte del giorno dopo l'ultimo), dove la data di end è l'ultimo giorno incluso.
     */
    public static LocalDateTime[] normalizeInterval(LocalDateTime start, LocalDateTime end, boolean allDay) {
        if (end.isBefore(start)) {
            throw new ValidationException("La data di fine non può precedere quella di inizio");
        }
        if (!allDay) {
            return new LocalDateTime[]{start, end};
        }
        return new LocalDateTime[]{
                start.toLocalDate().atStartOfDay(),
                end.toLocalDate().plusDays(1).atStartOfDay()
        };
    }

    private void apply(CalendarEvent event, CalendarEventRequest request) {
        LocalDateTime[] interval = normalizeInterval(request.startAt(), request.endAt(), request.allDay());

        event.setTitle(request.title().trim());
        event.setDescription(blankToNull(request.description()));
        event.setLocation(blankToNull(request.location()));
        event.setStartAt(interval[0]);
        event.setEndAt(interval[1]);
        event.setAllDay(request.allDay());
        event.setParticipants(resolveParticipants(request.participantIds()));
        event.setWork(resolveWork(request.workId()));
    }

    private Set<User> resolveParticipants(List<UUID> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new ValidationException("Serve almeno un partecipante");
        }
        Set<UUID> ids = new LinkedHashSet<>(participantIds);
        if (ids.contains(null)) {
            throw new ValidationException("Partecipante non valido");
        }
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size() || users.stream().anyMatch(u -> u.getDeletedAt() != null)) {
            throw new ValidationException("Uno o più partecipanti non esistono o non sono attivi");
        }
        return new HashSet<>(users);
    }

    private Work resolveWork(UUID workId) {
        if (workId == null) {
            return null;
        }
        return workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));
    }

    private CalendarEvent findEvent(UUID id) {
        return calendarEventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Calendar event not found with id: " + id));
    }

    private void checkCanEdit(CalendarEvent event, User caller) {
        if (!canEdit(event, caller)) {
            throw new ForbiddenException("Solo il creatore o un amministratore può modificare questo impegno");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CalendarEventResponse toResponse(CalendarEvent event, User caller) {
        List<CalendarParticipantResponse> participants = event.getParticipants().stream()
                .sorted(BY_NAME)
                .map(this::toParticipant)
                .toList();

        return new CalendarEventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getStartAt(),
                event.getEndAt(),
                event.isAllDay(),
                participants,
                toParticipant(event.getCreatedBy()),
                toWorkRef(event.getWork()),
                canEdit(event, caller),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    private CalendarParticipantResponse toParticipant(User user) {
        return new CalendarParticipantResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                (user.getFirstName() + " " + user.getLastName()).trim(),
                user.getCalendarColor()
        );
    }

    private CalendarWorkRefResponse toWorkRef(Work work) {
        if (work == null) {
            return null;
        }
        StringBuilder label = new StringBuilder();
        if (work.getOrderNumber() != null && !work.getOrderNumber().isBlank()) {
            label.append(work.getOrderNumber());
        }
        String detail = work.getAtixClient() != null ? work.getAtixClient().getName() : work.getName();
        if (detail != null && !detail.isBlank()) {
            if (!label.isEmpty()) {
                label.append(" - ");
            }
            label.append(detail);
        }
        return new CalendarWorkRefResponse(work.getId(), label.isEmpty() ? work.getName() : label.toString());
    }
}
