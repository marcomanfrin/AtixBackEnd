package marcomanfrin.atixbackend.DTO.works;

import marcomanfrin.atixbackend.DTO.clients.ClientResponse;
import marcomanfrin.atixbackend.DTO.plants.PlantResponse;
import marcomanfrin.atixbackend.DTO.tickets.TicketResponse;
import marcomanfrin.atixbackend.DTO.users.UserSummaryDTO;
import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceResponse;

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
