package marcomanfrin.atixbackend.DTO.works;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record WorkRequest(
        @NotBlank(message = "Work name is required")
        @Size(min = 2, max = 100, message = "Work name must be between 2 and 100 characters")
        String name,

        String description,

        // F7: bid number (numero offerta) is now optional
        String bidNumber,

        UUID sellerId,

        @NotBlank(message = "Order number is required")
        String orderNumber,

        @NotNull(message = "Order date is required")
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

        Integer expectedOfficeHours,

        Integer expectedPlantHours,

        UUID ticketId
) {
}
