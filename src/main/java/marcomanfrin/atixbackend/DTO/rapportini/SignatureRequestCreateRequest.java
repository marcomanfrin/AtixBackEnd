package marcomanfrin.atixbackend.DTO.rapportini;

import jakarta.validation.constraints.Min;

public record SignatureRequestCreateRequest(
        @Min(value = 1, message = "Expiry must be at least 1 minute")
        Integer expiresInMinutes   // Optional - default 60, clamped to the server maximum
) {
}
