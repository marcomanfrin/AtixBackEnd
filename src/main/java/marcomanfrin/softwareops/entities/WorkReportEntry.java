package marcomanfrin.softwareops.entities;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hours = BigDecimal.ZERO;

    public WorkReportEntry() {}

    public WorkReportEntry(WorkReport report, Task task, BigDecimal hours) {
        this.report = report;
        this.task = task;
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

    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
    }

    public BigDecimal getHours() {
        return hours;
    }
    public void setHours(BigDecimal hours) {
        this.hours = (hours == null) ? BigDecimal.ZERO : hours;
    }
}