package marcomanfrin.softwareops.DTO.works;

import marcomanfrin.softwareops.DTO.clients.ClientResponse;
import marcomanfrin.softwareops.DTO.plants.PlantResponse;
import marcomanfrin.softwareops.DTO.tickets.TicketResponse;
import marcomanfrin.softwareops.DTO.users.UserSummaryDTO;
import marcomanfrin.softwareops.DTO.worksiteReferences.WorksiteReferenceResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkDetailResponse(
        UUID id,
        String name,
        String bidNumber,
        UserSummaryDTO seller,
        String orderNumber,
        LocalDate orderDate,
        Integer electricalSchemaProgression,
        Integer programmingProgression,
        LocalDate expectedStartDate,
        boolean completed,
        LocalDateTime completedAt,
        boolean invoiced,
        LocalDateTime invoicedAt,
        PlantResponse plant,
        ClientResponse atixClient,
        ClientResponse finalClient,
        List<WorksiteReferenceResponse> worksiteReferences,
        String nasSubDirectory,
        Integer expectedOfficeHours,
        Integer expectedPlantHours,
        TicketResponse ticket
) {
}
