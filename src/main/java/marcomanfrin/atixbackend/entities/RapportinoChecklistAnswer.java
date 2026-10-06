package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;

import java.util.UUID;

// Risposta checklist: conserva lo snapshot dell'etichetta, cosi' modificare il template
// non riscrive i rapportini gia' compilati.
@Entity
@Table(name = "rapportino_checklist_answers")
public class RapportinoChecklistAnswer {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rapportino_id", nullable = false)
    private Rapportino rapportino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_item_id")
    private ChecklistTemplateItem templateItem;

    @Column(nullable = false)
    private String labelSnapshot;

    @Column(nullable = false)
    private boolean checked;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    private int position;

    public RapportinoChecklistAnswer() {}

    public RapportinoChecklistAnswer(ChecklistTemplateItem templateItem, String labelSnapshot, boolean checked, String note, int position) {
        this.templateItem = templateItem;
        this.labelSnapshot = labelSnapshot;
        this.checked = checked;
        this.note = note;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public Rapportino getRapportino() {
        return rapportino;
    }
    public void setRapportino(Rapportino rapportino) {
        this.rapportino = rapportino;
    }

    public ChecklistTemplateItem getTemplateItem() {
        return templateItem;
    }
    public void setTemplateItem(ChecklistTemplateItem templateItem) {
        this.templateItem = templateItem;
    }

    public String getLabelSnapshot() {
        return labelSnapshot;
    }
    public void setLabelSnapshot(String labelSnapshot) {
        this.labelSnapshot = labelSnapshot;
    }

    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
}
