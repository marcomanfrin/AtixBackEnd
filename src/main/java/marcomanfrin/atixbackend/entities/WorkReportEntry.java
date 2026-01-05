package marcomanfrin.atixbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import marcomanfrin.atixbackend.entities.users.TechnicianUser;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private TechnicianUser technician;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours = BigDecimal.ZERO;

    @Column(nullable = true) // TODO: set to false
    private LocalDate date;

    public WorkReportEntry() {}

    public WorkReportEntry(WorkReport report, String description, BigDecimal hours, LocalDate date, TechnicianUser technician) {
        this.report = report;
        this.description = description;
        this.hours = hours;
        this.date = date;
        this.technician = technician;
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

    public TechnicianUser getTechnician() {
        return technician;
    }

    public void setTechnician(TechnicianUser technician) {
        this.technician = technician;
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

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
}