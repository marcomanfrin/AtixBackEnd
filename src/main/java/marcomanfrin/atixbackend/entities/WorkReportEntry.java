package marcomanfrin.atixbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "work_report_entries")
public class WorkReportEntry {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    @JsonIgnore
    private WorkReport report;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours = BigDecimal.ZERO;

    public WorkReportEntry() {}

    public WorkReportEntry(WorkReport report, String description, BigDecimal hours) {
        this.report = report;
        this.description = description;
        this.hours = hours;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public WorkReport getReport() {
        return report;
    }
    public void setReport(WorkReport report) {
        this.report = report;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getHours() {
        return hours;
    }
    public void setHours(BigDecimal hours) {
        this.hours = (hours == null) ? BigDecimal.ZERO : hours;
    }
}