package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.RapportinoStatus;
import marcomanfrin.atixbackend.enums.RapportinoWorkState;
import marcomanfrin.atixbackend.enums.SignatureSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Rapportino di intervento: documento firmato dal cliente, figlio di Work (1:N).
// I campi snapshot (cliente, referente, impianto, ordine) sono copiati da Work alla creazione
// e congelati alla firma, cosi' un documento firmato non cambia se l'anagrafica viene modificata.
// La firma vive in RapportinoSignature (tabella separata) per non finire nelle liste.
@Entity
@Table(name = "rapportini")
public class Rapportino {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Column(nullable = false, unique = true, length = 32)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RapportinoStatus status = RapportinoStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate interventionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;

    // Tipo intervento
    @Column(nullable = false)
    private boolean typeMaintenance;

    @Column(nullable = false)
    private boolean typeCallOut;

    @Column(nullable = false)
    private boolean typeQuote;

    @Column(nullable = false)
    private boolean typeWarranty;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal workHours = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal travelHours = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal travelKm = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean meal;

    @Column(nullable = false)
    private boolean parking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RapportinoWorkState workState = RapportinoWorkState.IN_PROGRESS;

    // Anagrafica scelta tra le entita' dell'applicazione (precompilata dalla commessa)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id")
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worksite_reference_id")
    private WorksiteReference worksiteReference;

    // Snapshot anagrafico: ricalcolato dalle entita' scelte finche' DRAFT, congelato alla firma
    private String clientName;
    private String clientReference;
    private String plantLabel;
    private String orderNumber;

    // Lingua del documento PDF ("it" / "en")
    @Column(nullable = false, length = 5)
    private String locale = "it";

    // Firma
    private String signerName;
    private LocalDateTime signedAt;
    private LocalDateTime privacyAcceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private SignatureSource signatureSource;

    // Documento PDF archiviato (Attachment con target REPORT)
    private UUID pdfAttachmentId;

    @Column(length = 64)
    private String pdfHash;

    // Annullamento
    private LocalDateTime voidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voided_by_id")
    private User voidedBy;

    // Rapportino annullato che questo sostituisce (void-and-reissue)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaces_id")
    private Rapportino replaces;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "rapportino", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<RapportinoChecklistAnswer> checklistAnswers = new ArrayList<>();

    @OneToMany(mappedBy = "rapportino", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<RapportinoMaterial> materials = new ArrayList<>();

    public Rapportino() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDraft() {
        return status == RapportinoStatus.DRAFT;
    }

    public BigDecimal getTotalHours() {
        return workHours.add(travelHours);
    }

    public void replaceChecklistAnswers(List<RapportinoChecklistAnswer> answers) {
        this.checklistAnswers.clear();
        for (RapportinoChecklistAnswer answer : answers) {
            answer.setRapportino(this);
            this.checklistAnswers.add(answer);
        }
    }

    public void replaceMaterials(List<RapportinoMaterial> newMaterials) {
        this.materials.clear();
        for (RapportinoMaterial material : newMaterials) {
            material.setRapportino(this);
            this.materials.add(material);
        }
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public Work getWork() {
        return work;
    }
    public void setWork(Work work) {
        this.work = work;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public RapportinoStatus getStatus() {
        return status;
    }
    public void setStatus(RapportinoStatus status) {
        this.status = status;
    }

    public LocalDate getInterventionDate() {
        return interventionDate;
    }
    public void setInterventionDate(LocalDate interventionDate) {
        this.interventionDate = interventionDate;
    }

    public User getTechnician() {
        return technician;
    }
    public void setTechnician(User technician) {
        this.technician = technician;
    }

    public boolean isTypeMaintenance() {
        return typeMaintenance;
    }
    public void setTypeMaintenance(boolean typeMaintenance) {
        this.typeMaintenance = typeMaintenance;
    }

    public boolean isTypeCallOut() {
        return typeCallOut;
    }
    public void setTypeCallOut(boolean typeCallOut) {
        this.typeCallOut = typeCallOut;
    }

    public boolean isTypeQuote() {
        return typeQuote;
    }
    public void setTypeQuote(boolean typeQuote) {
        this.typeQuote = typeQuote;
    }

    public boolean isTypeWarranty() {
        return typeWarranty;
    }
    public void setTypeWarranty(boolean typeWarranty) {
        this.typeWarranty = typeWarranty;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }
    public void setWorkHours(BigDecimal workHours) {
        this.workHours = (workHours == null) ? BigDecimal.ZERO : workHours;
    }

    public BigDecimal getTravelHours() {
        return travelHours;
    }
    public void setTravelHours(BigDecimal travelHours) {
        this.travelHours = (travelHours == null) ? BigDecimal.ZERO : travelHours;
    }

    public BigDecimal getTravelKm() {
        return travelKm;
    }
    public void setTravelKm(BigDecimal travelKm) {
        this.travelKm = (travelKm == null) ? BigDecimal.ZERO : travelKm;
    }

    public boolean isMeal() {
        return meal;
    }
    public void setMeal(boolean meal) {
        this.meal = meal;
    }

    public boolean isParking() {
        return parking;
    }
    public void setParking(boolean parking) {
        this.parking = parking;
    }

    public RapportinoWorkState getWorkState() {
        return workState;
    }
    public void setWorkState(RapportinoWorkState workState) {
        this.workState = workState;
    }

    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }

    public Plant getPlant() {
        return plant;
    }
    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public WorksiteReference getWorksiteReference() {
        return worksiteReference;
    }
    public void setWorksiteReference(WorksiteReference worksiteReference) {
        this.worksiteReference = worksiteReference;
    }

    public String getClientName() {
        return clientName;
    }
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientReference() {
        return clientReference;
    }
    public void setClientReference(String clientReference) {
        this.clientReference = clientReference;
    }

    public String getPlantLabel() {
        return plantLabel;
    }
    public void setPlantLabel(String plantLabel) {
        this.plantLabel = plantLabel;
    }

    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getSignerName() {
        return signerName;
    }
    public void setSignerName(String signerName) {
        this.signerName = signerName;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }
    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public LocalDateTime getPrivacyAcceptedAt() {
        return privacyAcceptedAt;
    }
    public void setPrivacyAcceptedAt(LocalDateTime privacyAcceptedAt) {
        this.privacyAcceptedAt = privacyAcceptedAt;
    }

    public SignatureSource getSignatureSource() {
        return signatureSource;
    }
    public void setSignatureSource(SignatureSource signatureSource) {
        this.signatureSource = signatureSource;
    }

    public UUID getPdfAttachmentId() {
        return pdfAttachmentId;
    }
    public void setPdfAttachmentId(UUID pdfAttachmentId) {
        this.pdfAttachmentId = pdfAttachmentId;
    }

    public String getPdfHash() {
        return pdfHash;
    }
    public void setPdfHash(String pdfHash) {
        this.pdfHash = pdfHash;
    }

    public LocalDateTime getVoidedAt() {
        return voidedAt;
    }
    public void setVoidedAt(LocalDateTime voidedAt) {
        this.voidedAt = voidedAt;
    }

    public User getVoidedBy() {
        return voidedBy;
    }
    public void setVoidedBy(User voidedBy) {
        this.voidedBy = voidedBy;
    }

    public Rapportino getReplaces() {
        return replaces;
    }
    public void setReplaces(Rapportino replaces) {
        this.replaces = replaces;
    }

    public User getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<RapportinoChecklistAnswer> getChecklistAnswers() {
        return checklistAnswers;
    }

    public List<RapportinoMaterial> getMaterials() {
        return materials;
    }
}
