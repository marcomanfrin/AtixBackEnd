package marcomanfrin.softwareops.ServiceInterfaces;

import marcomanfrin.softwareops.DTO.dashboard.DashboardSummaryDTO;

public interface IDashboardService {
    public DashboardSummaryDTO getSummary(int limit);
}
