package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.tickets.CreateTicketRequest;
import marcomanfrin.softwareops.DTO.tickets.UpdateTicketRequest;
import marcomanfrin.softwareops.entities.Client;
import marcomanfrin.softwareops.entities.Plant;
import marcomanfrin.softwareops.entities.Ticket;
import marcomanfrin.softwareops.enums.TicketStatus;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.ClientRepository;
import marcomanfrin.softwareops.repositories.PlantRepository;
import marcomanfrin.softwareops.repositories.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService implements ITicketService {

    private final TicketRepository ticketRepository;
    private final ClientRepository clientRepository;
    private final PlantRepository plantRepository;

    public TicketService(TicketRepository ticketRepository, ClientRepository clientRepository, PlantRepository plantRepository) {
        this.ticketRepository = ticketRepository;
        this.clientRepository = clientRepository;
        this.plantRepository = plantRepository;
    }

    @Override
    @Transactional
    public Ticket createTicket(CreateTicketRequest request) {
        String name = validateName(request.name());

        Ticket ticket = new Ticket();
        ticket.setName(name);
        ticket.setDescription(request.description());
        ticket.setStatus(TicketStatus.OPEN);

        // Collega client se fornito
        if (request.clientId() != null) {
            Client client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new NotFoundException("Client not found: " + request.clientId()));
            ticket.setClient(client);
        }

        // Collega plant se fornito
        if (request.plantId() != null) {
            Plant plant = plantRepository.findById(request.plantId())
                    .orElseThrow(() -> new NotFoundException("Plant not found: " + request.plantId()));
            ticket.setPlant(plant);
        }

        // I timestamp sono gestiti automaticamente da @PrePersist/@PreUpdate
        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket getTicketOrThrow(UUID id) {
        return ticketRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Page<Ticket> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public Ticket patchTicket(UUID id, UpdateTicketRequest request) {
        Ticket ticket = getTicketOrThrow(id);

        boolean changed = false;

        if (request.name() != null) {
            ticket.setName(validateName(request.name()));
            changed = true;
        }

        if (request.description() != null) {
            ticket.setDescription(request.description());
            changed = true;
        }

        if (request.status() != null && request.status() != ticket.getStatus()) {
            applyStatusChange(ticket, request.status());
            changed = true;
        }

        if (changed) {
            ticket.setUpdatedAt(LocalDateTime.now());
            return ticketRepository.save(ticket);
        }

        return ticket;
    }

    @Override
    public void deleteTicket(UUID id) {
        if (!ticketRepository.existsById(id)) throw new NotFoundException(id);
        ticketRepository.deleteById(id);
    }

    @Override
    public Ticket changeTicketStatus(UUID id, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));

        if (ticket.getStatus() == status) {
            return ticket;
        }
        ticket.setStatus(status);

         if (status == TicketStatus.CLOSED && ticket.getResolvedAt() == null) {
             ticket.setResolvedAt(LocalDateTime.now());
         }

        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket closeTicket(UUID ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        if (ticket.getStatus() == TicketStatus.CLOSED) return ticket;

        applyStatusChange(ticket, TicketStatus.CLOSED);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    @Override
    public List<Ticket> getTicketsByClient(UUID clientId) {
        return ticketRepository.findByClient_Id(clientId);
    }

    @Override
    public List<Ticket> getTicketsByPlant(UUID plantId) {
        return ticketRepository.findByPlant_Id(plantId);
    }

    @Override
    public Page<Ticket> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByClient(UUID clientId, Pageable pageable) {
        return ticketRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByPlant(UUID plantId, Pageable pageable) {
        return ticketRepository.findByPlant_Id(plantId, pageable);
    }

    private void applyStatusChange(Ticket ticket, TicketStatus nextStatus) {
        ticket.setStatus(nextStatus);
        if (nextStatus == TicketStatus.CLOSED && ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
    }

    private String validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Ticket name cannot be blank");
        }
        return name.trim();
    }
}
