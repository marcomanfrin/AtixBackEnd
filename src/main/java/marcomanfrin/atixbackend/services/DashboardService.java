package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.dashboard.*;
import marcomanfrin.atixbackend.ServiceInterfaces.IDashboardService;
import marcomanfrin.atixbackend.enums.TicketStatus;
import marcomanfrin.atixbackend.enums.WorkStatus;
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

        int completedWorksCount = (int) workRepository.countCompleted();
        int pendingWorksCount = (int) workRepository.countPending();

        List<WorkStatusCountDTO> worksByStatus = Arrays.stream(WorkStatus.values())
                .map(s -> new WorkStatusCountDTO(s, (int) workRepository.countByStatus(s)))
                .toList();

        List<TicketStatusCountDTO> ticketsByStatus = Arrays.stream(TicketStatus.values())
                .map(s -> new TicketStatusCountDTO(s, (int) ticketRepository.countByStatus(s)))
                .toList();

        var page = PageRequest.of(0, limit);

        List<WorkPreviewDTO> lastWorks = workRepository.findAllByOrderByCreatedAtDesc(page).stream()
                .map(w -> new WorkPreviewDTO(w.getId(), w.getName(), w.getStatus(), w.getCreatedAt()))
                .toList();

        List<TicketPreviewDTO> lastTickets = ticketRepository.findAllByOrderByCreatedAtDesc(page).stream()
                .map(t -> new TicketPreviewDTO(t.getId(), t.getName(), t.getStatus(), t.getCreatedAt()))
                .toList();

        return new DashboardSummaryDTO(
                clientsCount,
                plantsCount,
                completedWorksCount,
                pendingWorksCount,
                worksByStatus,
                ticketsByStatus,
                lastWorks,
                lastTickets
        );
    }
}
