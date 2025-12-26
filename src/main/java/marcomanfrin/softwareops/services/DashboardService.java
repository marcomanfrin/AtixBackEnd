package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.dashboard.*;
import marcomanfrin.softwareops.enums.TicketStatus;
import marcomanfrin.softwareops.enums.WorkStatus;
import marcomanfrin.softwareops.repositories.ClientRepository;
import marcomanfrin.softwareops.repositories.PlantRepository;
import marcomanfrin.softwareops.repositories.TicketRepository;
import marcomanfrin.softwareops.repositories.WorkRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DashboardService {

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

    public DashboardSummaryDTO getSummary(int limit) {
        int safeLimit = (limit < 1 || limit > 50) ? 5 : limit;

        int clientsCount = (int) clientRepository.count();
        int plantsCount = (int) plantRepository.count();

        List<WorkStatusCountDTO> worksByStatus = Arrays.stream(WorkStatus.values())
                .map(s -> new WorkStatusCountDTO(s, (int) workRepository.countByStatus(s)))
                .toList();

        List<TicketStatusCountDTO> ticketsByStatus = Arrays.stream(TicketStatus.values())
                .map(s -> new TicketStatusCountDTO(s, (int) ticketRepository.countByStatus(s)))
                .toList();

        var page = PageRequest.of(0, safeLimit);

        List<WorkPreviewDTO> lastWorks = workRepository.findAllByOrderByCreatedAtDesc(page).stream()
                .map(w -> new WorkPreviewDTO(w.getId(), w.getStatus(), w.getCreatedAt()))
                .toList();

        List<TicketPreviewDTO> lastTickets = ticketRepository.findAllByOrderByCreatedAtDesc(page).stream()
                .map(t -> new TicketPreviewDTO(t.getId(), t.getStatus(), t.getCreatedAt()))
                .toList();

        return new DashboardSummaryDTO(
                clientsCount,
                plantsCount,
                worksByStatus,
                ticketsByStatus,
                lastWorks,
                lastTickets
        );
    }
}