package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.calendar.CalendarEventRequest;
import marcomanfrin.atixbackend.DTO.calendar.CalendarEventResponse;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.services.CalendarEventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/calendar-events")
public class CalendarEventsController {
    private final CalendarEventService calendarEventService;

    public CalendarEventsController(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    @GetMapping
    public ResponseEntity<List<CalendarEventResponse>> getEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "false") boolean mine,
            @RequestParam(required = false) List<UUID> participantIds,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(calendarEventService.listEvents(from, to, mine, participantIds, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarEventResponse> getEventById(@PathVariable UUID id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(calendarEventService.getEvent(id, user));
    }

    @PostMapping
    public ResponseEntity<CalendarEventResponse> createEvent(
            @Valid @RequestBody CalendarEventRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarEventService.createEvent(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CalendarEventRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(calendarEventService.updateEvent(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        calendarEventService.deleteEvent(id, user);
        return ResponseEntity.noContent().build();
    }
}
