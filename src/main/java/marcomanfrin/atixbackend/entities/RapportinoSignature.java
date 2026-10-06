package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

// Immagine della firma in tabella separata: possiede la FK verso il rapportino, che non la mappa,
// quindi viene caricata solo esplicitamente (dettaglio e PDF), mai nelle liste.
@Entity
@Table(name = "rapportino_signatures")
public class RapportinoSignature {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rapportino_id", nullable = false, unique = true)
    private Rapportino rapportino;

    // PNG come data URL (data:image/png;base64,...)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageData;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    @Column(length = 64)
    private String signerIp;

    @Column(length = 512)
    private String signerUserAgent;

    public RapportinoSignature() {}

    public RapportinoSignature(Rapportino rapportino, String imageData, LocalDateTime capturedAt, String signerIp, String signerUserAgent) {
        this.rapportino = rapportino;
        this.imageData = imageData;
        this.capturedAt = capturedAt;
        this.signerIp = signerIp;
        this.signerUserAgent = signerUserAgent;
    }

    public UUID getId() {
        return id;
    }

    public Rapportino getRapportino() {
        return rapportino;
    }

    public String getImageData() {
        return imageData;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public String getSignerIp() {
        return signerIp;
    }

    public String getSignerUserAgent() {
        return signerUserAgent;
    }
}
