package marcomanfrin.softwareops.controllers;

import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.tickets.CreateTicketRequest;
import marcomanfrin.softwareops.DTO.tickets.TicketResponse;
import marcomanfrin.softwareops.DTO.tickets.UpdateTicketRequest;
import marcomanfrin.softwareops.entities.Ticket;
import marcomanfrin.softwareops.entities.Work;
import marcomanfrin.softwareops.enums.TicketStatus;
import marcomanfrin.softwareops.services.ITicketService;
import marcomanfrin.softwareops.services.IWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketsController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ITicketService ticketService;
    private final IWorkService workService;

    public TicketsController(ITicketService ticketService, IWorkService workService) {
        this.ticketService = ticketService;
        this.workService = workService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(request);
        return toResponse(ticket);
    }

    @GetMapping
    public Page<TicketResponse> getAllTickets(
            @RequestParam(name = "status", required = false) TicketStatus status,
            @RequestParam(name = "clientId", required = false) UUID clientId,
            @RequestParam(name = "plantId", required = false) UUID plantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        PageRequest pageRequest = buildPageRequest(page, size, Sort.by("createdAt").descending());
        Page<Ticket> tickets;

        if (status != null) {
            tickets = ticketService.getTicketsByStatus(status, pageRequest);
        } else if (clientId != null) {
            tickets = ticketService.getTicketsByClient(clientId, pageRequest);
        } else if (plantId != null) {
            tickets = ticketService.getTicketsByPlant(plantId, pageRequest);
        } else {
            tickets = ticketService.getAllTickets(pageRequest);
        }

        return tickets.map(this::toResponse);
    }


    @GetMapping("/{id}")
    public TicketResponse getTicketById(@PathVariable UUID id) {
        return toResponse(ticketService.getTicketOrThrow(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@securityService.isTechnician(authentication)")
    public TicketResponse patchTicket(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return toResponse(ticketService.patchTicket(id, request));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("@securityService.isTechnician(authentication)")
    public TicketResponse closeTicket(@PathVariable UUID id) {
        return toResponse(ticketService.closeTicket(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public void deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
    }

    @PostMapping("/{id}/works")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@securityService.isTechnician(authentication)")
    public Work createWorkFromTicket(@PathVariable UUID id) {
        return workService.createWorkFromTicket(id);
    }

    private PageRequest buildPageRequest(int page, int size, Sort sort) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;
        return PageRequest.of(safePage, safeSize, sort);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getName(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                ticket.getClient() != null ? ticket.getClient().getId() : null,
                ticket.getPlant() != null ? ticket.getPlant().getId() : null
        );
    }
}
