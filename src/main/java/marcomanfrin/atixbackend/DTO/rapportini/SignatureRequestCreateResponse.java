package marcomanfrin.atixbackend.DTO.rapportini;

import java.time.LocalDateTime;

// Il token in chiaro viene restituito solo qui, una volta; il frontend costruisce l'URL /sign/{token}
public record SignatureRequestCreateResponse(
        String token,
        LocalDateTime expiresAt
) {
}
