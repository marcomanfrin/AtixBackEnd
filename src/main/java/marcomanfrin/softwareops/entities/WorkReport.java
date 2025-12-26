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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false, unique = true)
    @JsonIgnore
    private Work work;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkReportEntry> entries = new ArrayList<>();

    public WorkReport() {}

    public WorkReport(Work work, BigDecimal totalHours, List<WorkReportEntry> entries) {
        this.work = work;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
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