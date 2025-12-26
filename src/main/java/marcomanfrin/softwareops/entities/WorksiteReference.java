package marcomanfrin.softwareops.entities;

import jakarta.persistence.*;
import marcomanfrin.softwareops.enums.WorksiteReferenceRole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "worksite_references")
public class WorksiteReference {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "worksiteReference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorksiteReferenceAssignment> assignments = new ArrayList<>();

    public WorksiteReference() {
    }

    public WorksiteReference(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<WorksiteReferenceAssignment> getAssignments() {
        return assignments;
    }
    public void setAssignments(List<WorksiteReferenceAssignment> assignments) {
        this.assignments = assignments;
    }
}
