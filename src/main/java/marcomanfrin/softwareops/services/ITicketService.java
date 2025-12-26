package marcomanfrin.softwareops.services;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.tickets.CreateTicketRequest;
import marcomanfrin.softwareops.DTO.tickets.UpdateTicketRequest;
import marcomanfrin.softwareops.entities.Ticket;
import marcomanfrin.softwareops.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ITicketService {
    Ticket getTicketOrThrow(UUID id);
    List<Ticket> getAllTickets();
    Page<Ticket> getAllTickets(Pageable pageable);
    @Transactional
    Ticket patchTicket(UUID id, UpdateTicketRequest request);
    void deleteTicket(UUID id);

    Ticket changeTicketStatus(UUID id, TicketStatus status);

    Ticket closeTicket(UUID ticketId);
    List<Ticket> getTicketsByStatus(TicketStatus status);
    List<Ticket> getTicketsByClient(UUID clientId);
    List<Ticket> getTicketsByPlant(UUID plantId);
    Page<Ticket> getTicketsByStatus(TicketStatus status, Pageable pageable);
    Page<Ticket> getTicketsByClient(UUID clientId, Pageable pageable);
    Page<Ticket> getTicketsByPlant(UUID plantId, Pageable pageable);
    Ticket createTicket(@Valid CreateTicketRequest request);
}
