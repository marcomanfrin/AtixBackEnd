package marcomanfrin.softwareops.DTO.works;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record WorkRequest(
        @NotBlank(message = "Work name is required")
        @Size(min = 2, max = 100, message = "Work name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Bid number is required")
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

        @NotNull(message = "Atix client ID is required")
        UUID atixClientId,

        UUID finalClientId,

        @NotBlank(message = "NAS subdirectory is required")
        String nasSubDirectory,

        Integer expectedOfficeHours,

        Integer expectedPlantHours,

        UUID ticketId
) {
}
