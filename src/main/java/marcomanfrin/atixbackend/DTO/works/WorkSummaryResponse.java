package marcomanfrin.atixbackend.DTO.works;

import marcomanfrin.atixbackend.DTO.clients.ClientResponse;
import marcomanfrin.atixbackend.DTO.plants.PlantResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record WorkSummaryResponse(
        UUID id,
        String name,
        String bidNumber,
        String orderNumber,
        LocalDate orderDate,
        Boolean completed,
        Boolean invoiced,
        Integer electricalSchemaProgression,
        Integer programmingProgression,
        String nasSubDirectory,
        String relatedPlantNasDirectory,
        LocalDate expectedStartDate,
        PlantResponse plant,
        ClientResponse finalClient,
        List<WorkAssignmentResponse> assignedTechnicians
) {
}
