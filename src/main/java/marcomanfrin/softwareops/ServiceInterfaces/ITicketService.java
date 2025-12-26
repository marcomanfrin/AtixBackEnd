package marcomanfrin.softwareops.ServiceInterfaces;

import marcomanfrin.softwareops.DTO.tickets.TicketRequest;
import marcomanfrin.softwareops.DTO.tickets.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ITicketService {
    TicketResponse createTicket(TicketRequest request);
    Page<TicketResponse> getAllTickets(Pageable pageable);
    TicketResponse getTicketById(UUID id);
    TicketResponse updateTicket(UUID id, TicketRequest request);
    void deleteTicket(UUID id);
}
