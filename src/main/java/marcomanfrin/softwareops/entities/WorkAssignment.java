package marcomanfrin.softwareops.entities;

import jakarta.persistence.*;
import marcomanfrin.softwareops.enums.AssignmentRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "work_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_work_user",
                        columnNames = {"work_id", "user_id"}
                )
        }
)
public class WorkAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentRole assignmentRole;

    public WorkAssignment() {}

    public WorkAssignment(Work work, User user, AssignmentRole assignmentRole) {
        this.work = work;
        this.user = user;
        this.assignmentRole = assignmentRole;
    }

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
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

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public AssignmentRole getAssignmentRole() {
        return assignmentRole;
    }
    public void setAssignmentRole(AssignmentRole assignmentRole) {
        this.assignmentRole = assignmentRole;
    }
}