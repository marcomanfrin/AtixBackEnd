package marcomanfrin.softwareops.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "work_reports")
public class WorkReport {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Se il report è 1-per-work (tipico), usa OneToOne con unique.
     * Se invece vuoi più report per work, torna a ManyToOne.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false, unique = true)
    @JsonIgnore
    private Work work;

    /**
     * Nel tuo dominio: report quasi sempre legato a ticket (per WorkFromTicket),
     * ma lo lascio opzionale nel mapping per flessibilità.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ticket_id")
    @JsonIgnore
    private Ticket ticket;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean invoiced = false;

    private LocalDateTime invoicedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean finalized = false;

    private LocalDateTime finalizedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkReportEntry> entries = new ArrayList<>();

    public WorkReport() {}

    public WorkReport(Work work, Ticket ticket, BigDecimal totalHours, List<WorkReportEntry> entries) {
        this.work = work;
        this.ticket = ticket;
        this.totalHours = totalHours;
        this.entries = entries;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public Work getWork() {
        return work;
    }
    public void setWork(Work work) {
        this.work = work;
    }

    public Ticket getTicket() {
        return ticket;
    }
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }
    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }

    public boolean isInvoiced() {
        return invoiced;
    }
    public void setInvoiced(boolean invoiced) {
        this.invoiced = invoiced;
    }

    public LocalDateTime getInvoicedAt() {
        return invoicedAt;
    }
    public void setInvoicedAt(LocalDateTime invoicedAt) {
        this.invoicedAt = invoicedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFinalized() {
        return finalized;
    }
    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }
    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public List<WorkReportEntry> getEntries() {
        return entries;
    }
    public void setEntries(List<WorkReportEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(WorkReportEntry entry) {
        entries.add(entry);
        entry.setReport(this);
    }
    public void removeEntry(WorkReportEntry entry) {
        entries.remove(entry);
        entry.setReport(null);
    }
}