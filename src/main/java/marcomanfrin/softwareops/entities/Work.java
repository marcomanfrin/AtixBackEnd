package marcomanfrin.softwareops.entities;

import jakarta.persistence.*;
import marcomanfrin.softwareops.enums.WorkStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "works")
public abstract class Work {

    // Work ha senso come JOINED perché i sottotipi rappresentano concetti di dominio diversi
    // (lavoro da impianto vs da ticket) che possono evolvere con campi e logiche specifiche.
    //La strategia JOINED mantiene il database normalizzato, evitando colonne inutili
    // o NULL quando i sottotipi iniziano a divergere.

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status  = WorkStatus.DRAFT;

    @Column(nullable = false)
    private int progressPercent = 0;

    @OneToMany(mappedBy = "work", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<WorkAssignment> assignments = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Work() {}

    public Work(WorkStatus status, int progressPercent) {
        this.status = status;
        this.progressPercent = progressPercent;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getProgressPercent() {
        return progressPercent;
    }
    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public WorkStatus getStatus() {
        return status;
    }
    public void setStatus(WorkStatus status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public List<WorkAssignment> getAssignments() {
        return assignments;
    }
    public void setAssignments(List<WorkAssignment> assignments) {
        this.assignments = assignments;
    }
}
