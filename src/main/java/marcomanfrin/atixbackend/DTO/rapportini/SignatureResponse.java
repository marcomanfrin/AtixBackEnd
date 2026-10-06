package marcomanfrin.atixbackend.DTO.rapportini;

import marcomanfrin.atixbackend.enums.SignatureSource;

import java.time.LocalDateTime;

public record SignatureResponse(
        String imageData,
        String signerName,
        LocalDateTime signedAt,
        LocalDateTime privacyAcceptedAt,
        SignatureSource source
) {
}
