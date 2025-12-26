package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.dashboard.*;
import marcomanfrin.atixbackend.ServiceInterfaces.IDashboardService;
import marcomanfrin.atixbackend.enums.TicketStatus;
import marcomanfrin.atixbackend.repositories.ClientRepository;
import marcomanfrin.atixbackend.repositories.PlantRepository;
import marcomanfrin.atixbackend.repositories.TicketRepository;
import marcomanfrin.atixbackend.repositories.WorkRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DashboardService implements IDashboardService {

    private final WorkRepository workRepository;
    private final TicketRepository ticketRepository;
    private final PlantRepository plantRepository;
    private final ClientRepository clientRepository;

    public DashboardService(
            WorkRepository workRepository,
            TicketRepository ticketRepository,
            PlantRepository plantRepository,
            ClientRepository clientRepository
    ) {
        this.workRepository = workRepository;
        this.ticketRepository = ticketRepository;
        this.plantRepository = plantRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public DashboardSummaryDTO getSummary(int limit) {
        int clientsCount = (int) clientRepository.count();
        int plantsCount = (int) plantRepository.count();

        int completedWorksCount = (int) workRepository.countByCompleted(true);
        int pendingWorksCount = (int) workRepository.countByCompleted(false);

        List<TicketStatusCountDTO> ticketsByStatus = Arrays.stream(TicketStatus.values())
                .map(s -> new TicketStatusCountDTO(s, (int) ticketRepository.countByStatus(s)))
                .toList();

        var page = PageRequest.of(0, limit);

        List<WorkPreviewDTO> lastWorks = workRepository.findAllByOrderByCreatedAtDesc(page).stream()
                .map(w -> new WorkPreviewDTO(w.getId(), w.getName(), w.isCompleted(), w.getCreatedAt()))
                .toList();

        List<TicketPreviewDTO> lastTickets = ticketRepository.findAllByOrderByCreatedAtDesc(page).stream()
                .map(t -> new TicketPreviewDTO(t.getId(), t.getName(), t.getStatus(), t.getCreatedAt()))
                .toList();

        return new DashboardSummaryDTO(
                clientsCount,
                plantsCount,
                completedWorksCount,
                pendingWorksCount,
                ticketsByStatus,
                lastWorks,
                lastTickets
        );
    }
}