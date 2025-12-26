package marcomanfrin.atixbackend.resolvers;

import marcomanfrin.atixbackend.DTO.dashboard.DashboardSummaryDTO;
import marcomanfrin.atixbackend.ServiceInterfaces.IDashboardService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardGraphQLController {

    private final IDashboardService dashboardService;

    public DashboardGraphQLController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'USER')")
    public DashboardSummaryDTO dashboardSummary(@Argument Integer limit) {
        int safeLimit = (limit == null || limit < 1 || limit > 50) ? 5 : limit;
        return dashboardService.getSummary(safeLimit);
    }
}