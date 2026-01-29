package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.tickets.TicketRequest;
import marcomanfrin.atixbackend.DTO.tickets.TicketResponse;
import marcomanfrin.atixbackend.DTO.tickets.TicketUpdateRequest;
import marcomanfrin.atixbackend.enums.TicketStatus;
import marcomanfrin.atixbackend.security.SecurityService;
import marcomanfrin.atixbackend.services.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketsController {
    private final TicketService ticketService;

    public TicketsController(TicketService ticketService, SecurityService securityService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    // TODO: @PreAuthorize("@securityService.isAdministrative(authentication) or hasRole('OWNER')")
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketRequest request) {
        TicketResponse ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getAllTickets(
            @RequestParam(required = false) String senderEmail,
            @RequestParam(required = false) UUID orderNumberId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtTo,
            Pageable pageable) {

        // If any filter is provided, use filtered search
        if (senderEmail != null || orderNumberId != null || name != null ||
            description != null || status != null ||
            createdAtFrom != null || createdAtTo != null) {

            Page<TicketResponse> tickets = ticketService.getFilteredTickets(
                    senderEmail, orderNumberId, name, description,
                    status, createdAtFrom, createdAtTo, pageable
            );
            return ResponseEntity.ok(tickets);
        }

        // Otherwise, return all tickets
        Page<TicketResponse> tickets = ticketService.getAllTickets(pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable UUID id) {
        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable UUID id,
            @Valid @RequestBody TicketUpdateRequest request) {
        TicketResponse ticket = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
