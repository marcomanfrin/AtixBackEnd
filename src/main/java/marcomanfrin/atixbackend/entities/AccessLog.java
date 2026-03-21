package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import marcomanfrin.atixbackend.entities.users.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "access_logs")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "session_id", unique = true)
    private UUID sessionId;

    @Column(name = "logout_timestamp")
    private LocalDateTime logoutTimestamp;

    @Column(name = "jwt_expires_at")
    private LocalDateTime jwtExpiresAt;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public AccessLog() {}

    public AccessLog(User user, String email, String ipAddress, String userAgent, boolean success, String failureReason, UUID sessionId, LocalDateTime jwtExpiresAt) {
        this.user = user;
        this.email = email;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.success = success;
        this.failureReason = failureReason;
        this.sessionId = sessionId;
        this.jwtExpiresAt = jwtExpiresAt;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getEmail() { return email; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public boolean isSuccess() { return success; }
    public String getFailureReason() { return failureReason; }
    public UUID getSessionId() { return sessionId; }
    public LocalDateTime getLogoutTimestamp() { return logoutTimestamp; }
    public void setLogoutTimestamp(LocalDateTime logoutTimestamp) { this.logoutTimestamp = logoutTimestamp; }
    public LocalDateTime getJwtExpiresAt() { return jwtExpiresAt; }
}
