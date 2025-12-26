package marcomanfrin.softwareops.entities;

import jakarta.persistence.*;
import marcomanfrin.softwareops.enums.WorksiteReferenceRole;

import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "worksite_references")
public class WorksiteReference {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

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

    @OneToMany(mappedBy = "worksiteReferences")
    private Collection<Work> work;

    public Collection<Work> getWork() {
        return work;
    }

    public void setWork(Collection<Work> work) {
        this.work = work;
    }
}
