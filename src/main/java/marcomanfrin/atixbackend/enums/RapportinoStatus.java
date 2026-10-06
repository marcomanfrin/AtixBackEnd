package marcomanfrin.atixbackend.enums;

// Ciclo di vita del rapportino: DRAFT -> (AWAITING_SIGNATURE) -> SIGNED -> VOID
public enum RapportinoStatus {
    DRAFT,
    AWAITING_SIGNATURE,
    SIGNED,
    VOID
}
