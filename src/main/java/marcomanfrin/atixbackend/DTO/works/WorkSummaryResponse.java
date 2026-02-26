package marcomanfrin.atixbackend.DTO.works;

import marcomanfrin.atixbackend.DTO.clients.ClientResponse;
import marcomanfrin.atixbackend.DTO.plants.PlantResponse;
import marcomanfrin.atixbackend.enums.WorkStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// F3: added atixClient field so the work card can display it after the order number
public record WorkSummaryResponse(
        UUID id,
        String name,
        String bidNumber,
        String orderNumber,
        LocalDate orderDate,
        WorkStatus status,
        Integer electricalSchemaProgression,
        Integer programmingProgression,
        String nasSubDirectory,
        String relatedPlantNasDirectory,
        LocalDate expectedStartDate,
        PlantResponse plant,
        ClientResponse atixClient,
        ClientResponse finalClient,
        List<WorkAssignmentResponse> assignedTechnicians
) {
}
