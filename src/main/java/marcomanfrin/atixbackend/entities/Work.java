package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import marcomanfrin.atixbackend.entities.users.SellerUser;

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

    @Column(nullable = false)
    private String bidNumber;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private SellerUser seller;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private int electricalSchemaProgression = 0;

    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private int programmingProgression = 0;

    @Column(nullable = true)
    private LocalDate expectedStartDate;

    @Column(nullable = false)
    private boolean completed = false;
    @Column(nullable = true)
    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private boolean invoiced = false;
    @Column(nullable = true)
    private LocalDateTime invoicedAt;

    @ManyToOne
    @JoinColumn(name = "plant_id")
    private Plant plant;

    @ManyToOne
    @JoinColumn(name = "atix_client_id")
    private Client atixClient;

    @ManyToOne
    @JoinColumn(name = "final_client_id")
    private Client finalClient;

    @ManyToMany
    @JoinTable(
        name = "work_worksite_references",
        joinColumns = @JoinColumn(name = "work_id"),
        inverseJoinColumns = @JoinColumn(name = "worksite_reference_id")
    )
    private List<WorksiteReference> worksiteReferences = new ArrayList<>();

    @Column(nullable = false)
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
    }

    private Work(String name,
                 String bidNumber,
                 SellerUser seller,
                 String orderNumber,
                 LocalDate orderDate,
                 int electricalSchemaProgression,
                 int programmingProgression,
                 LocalDate expectedStartDate,
                 boolean completed,
                 LocalDateTime completedAt,
                 boolean invoiced,
                 LocalDateTime invoicedAt,
                 Plant plant,
                 Client atixClient,
                 Client finalClient,
                 String nasSubDirectory,
                 int expectedOfficeHours,
                 int expectedPlantHours) {
        this.name = name;
        this.bidNumber = bidNumber;
        this.seller = seller;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.electricalSchemaProgression = electricalSchemaProgression;
        this.programmingProgression = programmingProgression;
        this.expectedStartDate = expectedStartDate;
        this.completed = completed;
        this.completedAt = completedAt;
        this.invoiced = invoiced;
        this.invoicedAt = invoicedAt;
        this.plant = plant;
        this.atixClient = atixClient;
        this.finalClient = finalClient;
        this.nasSubDirectory = nasSubDirectory;
        this.expectedOfficeHours = expectedOfficeHours;
        this.expectedPlantHours = expectedPlantHours;
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

    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isInvoiced() {
        return invoiced;
    }
    public void setInvoiced(boolean invoiced) {
        this.invoiced = invoiced;
    }

    public LocalDateTime getInvoicedAt() {
        return invoicedAt;
    }
    public void setInvoicedAt(LocalDateTime invoicedAt) {
        this.invoicedAt = invoicedAt;
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

    public List<WorksiteReference> getWorksiteReferences() {
        return worksiteReferences;
    }
    public void setWorksiteReferences(List<WorksiteReference> worksiteReferences) {
        this.worksiteReferences = worksiteReferences;
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
