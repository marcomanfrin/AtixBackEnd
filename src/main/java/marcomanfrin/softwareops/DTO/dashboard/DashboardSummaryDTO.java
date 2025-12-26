package marcomanfrin.softwareops.DTO.dashboard;

import java.util.List;

public record DashboardSummaryDTO(
        int clientsCount,
        int plantsCount,
        List<WorkStatusCountDTO> worksByStatus,
        List<TicketStatusCountDTO> ticketsByStatus,
        List<WorkPreviewDTO> lastWorks,
        List<TicketPreviewDTO> lastTickets
) {}

