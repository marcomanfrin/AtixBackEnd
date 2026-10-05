package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import marcomanfrin.atixbackend.entities.users.User;

import java.time.LocalDateTime;
import java.util.UUID;

// Richiesta di firma remota: il token in chiaro esiste solo nell'URL, qui si salva solo lo SHA-256.
@Entity
@Table(name = "rapportino_signature_requests")
public class RapportinoSignatureRequest {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rapportino_id", nullable = false)
    private Rapportino rapportino;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private LocalDateTime revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 64)
    private String signerIp;

    @Column(length = 512)
    private String signerUserAgent;

    public RapportinoSignatureRequest() {}

    public RapportinoSignatureRequest(Rapportino rapportino, String tokenHash, LocalDateTime expiresAt, User createdBy) {
        this.rapportino = rapportino;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdBy = createdBy;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isUsable(LocalDateTime now) {
        return usedAt == null && revokedAt == null && expiresAt.isAfter(now);
    }

    public UUID getId() {
        return id;
    }

    public Rapportino getRapportino() {
        return rapportino;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getSignerIp() {
        return signerIp;
    }
    public void setSignerIp(String signerIp) {
        this.signerIp = signerIp;
    }

    public String getSignerUserAgent() {
        return signerUserAgent;
    }
    public void setSignerUserAgent(String signerUserAgent) {
        this.signerUserAgent = signerUserAgent;
    }
}
