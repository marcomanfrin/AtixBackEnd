package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.dashboard.DashboardSummaryDTO;

public interface IDashboardService {
    public DashboardSummaryDTO getSummary(int limit);
}
