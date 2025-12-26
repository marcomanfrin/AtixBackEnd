package marcomanfrin.atixbackend.DTO.errors;

import java.time.LocalDateTime;

public record ErrorDTO(String message, LocalDateTime timestamp) {}
