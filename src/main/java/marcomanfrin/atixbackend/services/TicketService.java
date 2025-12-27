package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.tickets.TicketRequest;
import marcomanfrin.atixbackend.DTO.tickets.TicketResponse;
import marcomanfrin.atixbackend.DTO.tickets.TicketUpdateRequest;
import marcomanfrin.atixbackend.ServiceInterfaces.ITicketService;
import marcomanfrin.atixbackend.entities.Ticket;
import marcomanfrin.atixbackend.entities.Work;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.repositories.TicketRepository;
import marcomanfrin.atixbackend.repositories.WorkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService implements ITicketService {
    private final TicketRepository ticketRepository;
    private final WorkRepository workRepository;

    public TicketService(TicketRepository ticketRepository, WorkRepository workRepository) {
        this.ticketRepository = ticketRepository;
        this.workRepository = workRepository;
    }

    @Override
    @Transactional
    public TicketResponse createTicket(TicketRequest request) {
        Work orderNumber = null;
        if (request.orderNumberId() != null) {
            orderNumber = workRepository.findById(request.orderNumberId())
                    .orElseThrow(() -> new IllegalArgumentException("Work not found with id: " + request.orderNumberId()));
        }

        Ticket ticket = new Ticket(
                request.senderEmail(),
                orderNumber,
                request.name(),
                request.description(),
                request.status()
        );

        Ticket savedTicket = ticketRepository.save(ticket);
        return toTicketResponse(savedTicket);
    }

    @Override
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable)
                .map(this::toTicketResponse);
    }

    @Override
    public TicketResponse getTicketById(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found with id: " + id));
        return toTicketResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(UUID id, TicketUpdateRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found with id: " + id));

        // PATCH logic: update only non-null fields
        if (request.senderEmail() != null) {
            ticket.setSenderEmail(request.senderEmail());
        }
        if (request.orderNumberId() != null) {
            Work orderNumber = workRepository.findById(request.orderNumberId())
                    .orElseThrow(() -> new NotFoundException("Work not found with id: " + request.orderNumberId()));
            ticket.setOrderNumber(orderNumber);
        }
        if (request.name() != null) {
            ticket.setName(request.name());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.status() != null) {
            ticket.setStatus(request.status());
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        return toTicketResponse(updatedTicket);
    }

    @Override
    @Transactional
    public void deleteTicket(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new NotFoundException("Ticket not found with id: " + id);
        }
        ticketRepository.deleteById(id);
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSenderEmail(),
                ticket.getOrderNumber() != null ? ticket.getOrderNumber().getId() : null,
                ticket.getName(),
                ticket.getDescription(),
                ticket.getStatus()
        );
    }
}
