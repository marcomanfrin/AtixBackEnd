package marcomanfrin.atixbackend.DTO.rapportini;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Usato sia per la firma sul posto sia per quella remota (endpoint pubblico)
public record RapportinoSignRequest(
        @NotBlank(message = "Signer name is required")
        @Size(max = 255, message = "Signer name must be at most 255 characters")
        String signerName,

        @NotBlank(message = "Signature image is required")
        String signatureImage,   // data:image/png;base64,...

        @AssertTrue(message = "Privacy consent is required")
        boolean privacyAccepted
) {
}
