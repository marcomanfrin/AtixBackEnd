package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import marcomanfrin.atixbackend.entities.users.SellerUser;
import marcomanfrin.atixbackend.enums.WorkStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "works")
public class Work {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    // F7: bidNumber is now nullable (numero offerta is optional)
    @Column(nullable = true)
    private String bidNumber;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private SellerUser seller;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = true)
    @Min(0)
    @Max(100)
    private Integer electricalSchemaProgression = 0;

    @Column(nullable = true)
    @Min(0)
    @Max(100)
    private Integer programmingProgression = 0;

    @Column(nullable = true)
    private LocalDate expectedStartDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status = WorkStatus.SCHEDULED;

    private LocalDateTime statusChangedAt;

    @Column(name = "status_changed_by")
    private UUID statusChangedBy;

    @Column(nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "plant_id")
    private Plant plant;

    @ManyToOne
    @JoinColumn(name = "atix_client_id")
    private Client atixClient;

    @ManyToOne
    @JoinColumn(name = "final_client_id")
    private Client finalClient;

    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorksiteReferenceAssignment> worksiteReferenceAssignments = new ArrayList<>();

    @Column(nullable = true)
    private String nasSubDirectory;

    private int expectedOfficeHours;

    private int expectedPlantHours;

    @OneToOne(mappedBy = "orderNumber")
    private Ticket ticket;

    @OneToOne(mappedBy = "work", cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkReport workReport;

    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkAssignment> assignments = new ArrayList<>();

    public Work() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.statusChangedAt == null) {
            this.statusChangedAt = LocalDateTime.now();
        }
    }

    // Status helper methods
    public boolean isScheduled() {
        return this.status == WorkStatus.SCHEDULED;
    }

    public boolean isInProgress() {
        return this.status == WorkStatus.IN_PROGRESS;
    }

    public boolean isClosed() {
        return this.status == WorkStatus.CLOSED;
    }

    public boolean isInvoiced() {
        return this.status == WorkStatus.INVOICED;
    }

    public boolean isCompleted() {
        return this.status == WorkStatus.CLOSED || this.status == WorkStatus.INVOICED;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getBidNumber() {
        return bidNumber;
    }
    public void setBidNumber(String bidNumber) {
        this.bidNumber = bidNumber;
    }

    public SellerUser getSeller() {
        return seller;
    }
    public void setSeller(SellerUser seller) {
        this.seller = seller;
    }

    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public int getElectricalSchemaProgression() {
        return electricalSchemaProgression;
    }
    public void setElectricalSchemaProgression(int electricalSchemaProgression) {
        this.electricalSchemaProgression = electricalSchemaProgression;
    }

    public int getProgrammingProgression() {
        return programmingProgression;
    }
    public void setProgrammingProgression(int programmingProgression) {
        this.programmingProgression = programmingProgression;
    }

    public LocalDate getExpectedStartDate() {
        return expectedStartDate;
    }
    public void setExpectedStartDate(LocalDate expectedStartDate) {
        this.expectedStartDate = expectedStartDate;
    }

    public WorkStatus getStatus() {
        return status;
    }
    public void setStatus(WorkStatus status) {
        this.status = status;
    }

    public LocalDateTime getStatusChangedAt() {
        return statusChangedAt;
    }
    public void setStatusChangedAt(LocalDateTime statusChangedAt) {
        this.statusChangedAt = statusChangedAt;
    }

    public UUID getStatusChangedBy() {
        return statusChangedBy;
    }
    public void setStatusChangedBy(UUID statusChangedBy) {
        this.statusChangedBy = statusChangedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Plant getPlant() {
        return plant;
    }
    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Client getAtixClient() {
        return atixClient;
    }
    public void setAtixClient(Client atixClient) {
        this.atixClient = atixClient;
    }

    public Client getFinalClient() {
        return finalClient;
    }
    public void setFinalClient(Client finalClient) {
        this.finalClient = finalClient;
    }

    public List<WorksiteReferenceAssignment> getWorksiteReferenceAssignments() {
        return worksiteReferenceAssignments;
    }
    public void setWorksiteReferenceAssignments(List<WorksiteReferenceAssignment> worksiteReferenceAssignments) {
        this.worksiteReferenceAssignments = worksiteReferenceAssignments;
    }

    public String getNasSubDirectory() {
        return nasSubDirectory;
    }
    public void setNasSubDirectory(String nasSubDirectory) {
        this.nasSubDirectory = nasSubDirectory;
    }

    public int getExpectedOfficeHours() {
        return expectedOfficeHours;
    }
    public void setExpectedOfficeHours(int expectedOfficeHours) {
        this.expectedOfficeHours = expectedOfficeHours;
    }

    public int getExpectedPlantHours() {
        return expectedPlantHours;
    }
    public void setExpectedPlantHours(int expectedPlantHours) {
        this.expectedPlantHours = expectedPlantHours;
    }

    public Ticket getTicket() {
        return ticket;
    }
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public WorkReport getWorkReport() {
        return workReport;
    }
    public void setWorkReport(WorkReport workReport) {
        this.workReport = workReport;
    }

    public List<WorkAssignment> getAssignments() {
        return assignments;
    }
    public void setAssignments(List<WorkAssignment> assignments) {
        this.assignments = assignments;
    }
}
