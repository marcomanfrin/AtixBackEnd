package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
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
    private String telephone;
    private String notes;

    @OneToMany(mappedBy = "worksiteReference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorksiteReferenceAssignment> assignments = new ArrayList<>();

    public WorksiteReference() {
    }

    public WorksiteReference(String name, String telephone, String notes) {
        this.name = name;
        this.telephone = telephone;
        this.notes = notes;
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
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

}
