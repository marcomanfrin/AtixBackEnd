package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;

import java.util.UUID;

// Voce del template checklist attivita' (seed in DataInitializer, nessuna CRUD admin per ora)
@Entity
@Table(name = "checklist_template_items")
public class ChecklistTemplateItem {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false)
    private String labelIt;

    @Column(nullable = false)
    private String labelEn;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean active = true;

    public ChecklistTemplateItem() {}

    public ChecklistTemplateItem(String code, String labelIt, String labelEn, int position) {
        this.code = code;
        this.labelIt = labelIt;
        this.labelEn = labelEn;
        this.position = position;
    }

    public String labelFor(String locale) {
        return "en".equalsIgnoreCase(locale) ? labelEn : labelIt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getLabelIt() {
        return labelIt;
    }
    public void setLabelIt(String labelIt) {
        this.labelIt = labelIt;
    }

    public String getLabelEn() {
        return labelEn;
    }
    public void setLabelEn(String labelEn) {
        this.labelEn = labelEn;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
