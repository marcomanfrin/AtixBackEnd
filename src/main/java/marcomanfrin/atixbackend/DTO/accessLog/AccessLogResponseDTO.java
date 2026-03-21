package marcomanfrin.atixbackend.DTO.accessLog;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccessLogResponseDTO(
        UUID id,
        UUID userId,
        String userFullName,
        String email,
        LocalDateTime timestamp,
        String ipAddress,
        String userAgent,
        boolean success,
        String failureReason,
        java.util.UUID sessionId,
        java.time.LocalDateTime logoutTimestamp,
        java.time.LocalDateTime jwtExpiresAt
) {}
