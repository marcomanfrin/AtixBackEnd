package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.tickets.TicketRequest;
import marcomanfrin.atixbackend.DTO.tickets.TicketResponse;
import marcomanfrin.atixbackend.DTO.tickets.TicketUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ITicketService {
    TicketResponse createTicket(TicketRequest request);
    Page<TicketResponse> getAllTickets(Pageable pageable);
    TicketResponse getTicketById(UUID id);
    TicketResponse updateTicket(UUID id, TicketUpdateRequest request);
    void deleteTicket(UUID id);
}
