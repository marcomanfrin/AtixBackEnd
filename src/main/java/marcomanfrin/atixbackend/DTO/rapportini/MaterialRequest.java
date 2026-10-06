package marcomanfrin.atixbackend.DTO.rapportini;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MaterialRequest(
        @NotBlank(message = "Material description is required")
        @Size(max = 500, message = "Material description must be at most 500 characters")
        String description,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
        BigDecimal quantity
) {
}
