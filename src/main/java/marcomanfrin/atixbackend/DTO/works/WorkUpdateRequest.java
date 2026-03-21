package marcomanfrin.atixbackend.DTO.works;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record WorkUpdateRequest(
        @Size(min = 2, max = 100, message = "Work name must be between 2 and 100 characters")
        String name,

        String description,

        String bidNumber,

        UUID sellerId,

        String orderNumber,

        LocalDate orderDate,

        @Min(value = 0, message = "Electrical schema progression must be between 0 and 100")
        @Max(value = 100, message = "Electrical schema progression must be between 0 and 100")
        Integer electricalSchemaProgression,

        @Min(value = 0, message = "Programming progression must be between 0 and 100")
        @Max(value = 100, message = "Programming progression must be between 0 and 100")
        Integer programmingProgression,

        LocalDate expectedStartDate,

        UUID plantId,

        UUID atixClientId,

        UUID finalClientId,

        String nasSubDirectory,

        Double expectedOfficeHours,

        Double expectedPlantHours,

        UUID ticketId
) {
}
