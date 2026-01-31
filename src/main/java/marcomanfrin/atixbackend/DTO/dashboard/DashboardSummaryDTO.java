package marcomanfrin.atixbackend.DTO.dashboard;

import java.util.List;

public record DashboardSummaryDTO(
        int clientsCount,
        int plantsCount,
        int completedWorksCount,
        int pendingWorksCount,
        List<WorkStatusCountDTO> worksByStatus,
        List<TicketStatusCountDTO> ticketsByStatus,
        List<WorkPreviewDTO> lastWorks,
        List<TicketPreviewDTO> lastTickets
) {}
