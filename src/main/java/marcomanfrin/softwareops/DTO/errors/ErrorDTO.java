package marcomanfrin.softwareops.DTO.errors;

import java.time.LocalDateTime;

public record ErrorDTO(String message, LocalDateTime timestamp) {}
