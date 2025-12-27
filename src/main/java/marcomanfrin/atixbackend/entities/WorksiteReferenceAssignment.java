package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;

import java.util.UUID;

@Entity
@Table(
    name = "worksite_reference_assignments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_work_worksite_reference",
            columnNames = {"work_id", "worksite_reference_id"}
        )
    }
)
public class WorksiteReferenceAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worksite_reference_id", nullable = false)
    private WorksiteReference worksiteReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorksiteReferenceRole role;

    public WorksiteReferenceAssignment() {}

    public WorksiteReferenceAssignment(Work work, WorksiteReference worksiteReference, WorksiteReferenceRole role) {
        this.work = work;
        this.worksiteReference = worksiteReference;
        this.role = role;
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

    public WorksiteReference getWorksiteReference() {
        return worksiteReference;
    }
    public void setWorksiteReference(WorksiteReference worksiteReference) {
        this.worksiteReference = worksiteReference;
    }

    public WorksiteReferenceRole getRole() {
        return role;
    }
    public void setRole(WorksiteReferenceRole role) {
        this.role = role;
    }
}