package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import marcomanfrin.atixbackend.entities.users.User;

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


    public WorkAssignment() {}

    public WorkAssignment(Work work, User user) {
        this.work = work;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }


    public UUID getId() {
        return id;
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
}