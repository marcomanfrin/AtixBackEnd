package marcomanfrin.atixbackend.DTO.dashboard;

import java.util.List;

public record DashboardSummaryDTO(
        int clientsCount,
        int plantsCount,
        int completedWorksCount,
        int pendingWorksCount,
        List<TicketStatusCountDTO> ticketsByStatus,
        List<WorkPreviewDTO> lastWorks,
        List<TicketPreviewDTO> lastTickets
) {}

