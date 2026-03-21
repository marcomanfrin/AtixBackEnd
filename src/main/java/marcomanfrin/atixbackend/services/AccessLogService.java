package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.DTO.accessLog.AccessLogResponseDTO;
import marcomanfrin.atixbackend.entities.AccessLog;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.repositories.AccessLogRepository;
import marcomanfrin.atixbackend.specifications.AccessLogSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    public AccessLogService(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    /**
     * Saves a login attempt. Returns the sessionId for successful logins, null for failures.
     */
    public UUID saveLog(User user, String email, String ipAddress, String userAgent, boolean success, String failureReason, LocalDateTime jwtExpiresAt) {
        UUID sessionId = success ? UUID.randomUUID() : null;
        AccessLog log = new AccessLog(user, email, ipAddress, userAgent, success, failureReason, sessionId, jwtExpiresAt);
        accessLogRepository.save(log);
        return sessionId;
    }

    @Transactional
    public void recordLogout(UUID sessionId) {
        accessLogRepository.findBySessionId(sessionId).ifPresent(log -> {
            log.setLogoutTimestamp(LocalDateTime.now());
            accessLogRepository.save(log);
        });
    }

    public Page<AccessLogResponseDTO> getAccessLogs(
            UUID userId,
            String email,
            Boolean success,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        Specification<AccessLog> spec = (root, query, cb) -> cb.conjunction();
        spec = spec.and(AccessLogSpecification.hasUserId(userId));
        spec = spec.and(AccessLogSpecification.hasEmail(email));
        spec = spec.and(AccessLogSpecification.hasSuccess(success));
        spec = spec.and(AccessLogSpecification.hasTimestampAfter(from));
        spec = spec.and(AccessLogSpecification.hasTimestampBefore(to));

        return accessLogRepository.findAll(spec, pageable).map(this::toDTO);
    }

    private AccessLogResponseDTO toDTO(AccessLog log) {
        User user = log.getUser();
        UUID userId = user != null ? user.getId() : null;
        String userFullName = user != null ? user.getFirstName() + " " + user.getLastName() : null;
        return new AccessLogResponseDTO(
                log.getId(),
                userId,
                userFullName,
                log.getEmail(),
                log.getTimestamp(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.isSuccess(),
                log.getFailureReason(),
                log.getSessionId(),
                log.getLogoutTimestamp(),
                log.getJwtExpiresAt()
        );
    }
}
