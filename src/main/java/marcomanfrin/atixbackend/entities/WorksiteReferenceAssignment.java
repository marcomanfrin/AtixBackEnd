package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;

import java.util.UUID;

@Entity
@Table(
    name = "worksite_reference_assignments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_worksite_reference_user",
            columnNames = {"worksite_reference_id", "user_id"}
        )
    }
)
public class WorksiteReferenceAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worksite_reference_id", nullable = false)
    private WorksiteReference worksiteReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorksiteReferenceRole role;

    public WorksiteReferenceAssignment() {}

    public WorksiteReferenceAssignment(WorksiteReference worksiteReference, User user, WorksiteReferenceRole role) {
        this.worksiteReference = worksiteReference;
        this.user = user;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public WorksiteReference getWorksiteReference() {
        return worksiteReference;
    }
    public void setWorksiteReference(WorksiteReference worksiteReference) {
        this.worksiteReference = worksiteReference;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public WorksiteReferenceRole getRole() {
        return role;
    }
    public void setRole(WorksiteReferenceRole role) {
        this.role = role;
    }
}