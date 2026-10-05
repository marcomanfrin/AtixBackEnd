package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rapportino_materials")
public class RapportinoMaterial {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rapportino_id", nullable = false)
    private Rapportino rapportino;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false)
    private int position;

    public RapportinoMaterial() {}

    public RapportinoMaterial(String description, BigDecimal quantity, int position) {
        this.description = description;
        this.quantity = quantity;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public Rapportino getRapportino() {
        return rapportino;
    }
    public void setRapportino(Rapportino rapportino) {
        this.rapportino = rapportino;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
}
