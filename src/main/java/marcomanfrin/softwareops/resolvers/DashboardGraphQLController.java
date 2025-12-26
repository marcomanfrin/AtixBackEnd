package marcomanfrin.softwareops.resolvers;

import marcomanfrin.softwareops.DTO.dashboard.DashboardSummaryDTO;
import marcomanfrin.softwareops.ServiceInterfaces.IDashboardService;
import marcomanfrin.softwareops.services.DashboardService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardGraphQLController {

    private final IDashboardService dashboardService;

    public DashboardGraphQLController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @QueryMapping
    public DashboardSummaryDTO dashboardSummary(@Argument Integer limit) {
        int safeLimit = (limit == null || limit < 1 || limit > 50) ? 5 : limit;
        return dashboardService.getSummary(safeLimit);
    }
}