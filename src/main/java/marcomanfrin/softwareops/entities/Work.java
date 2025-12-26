package marcomanfrin.softwareops.entities;

import jakarta.persistence.*;
import jdk.jfr.Percentage;
import marcomanfrin.softwareops.entities.users.SellerUser;

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
    private String Name;

    @Column(nullable = false)
    private String bidNumber;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private SellerUser seller;


    @Column(nullable = false)
    private String OrderNumber;

    @Column(nullable = false)
    private LocalDate OrderDate;

    @Column(nullable = false)
    @Percentage
    private int ElectricalSchemaProgression = 0;

    @Column(nullable = false)
    @Percentage
    private int ProgrammingProgression = 0;

    @Column(nullable = true)
    private LocalDate expectedStartDate;

    @Column(nullable = false)
    private boolean completed = false;
    @Column(nullable = true)
    private LocalDateTime completedAt;

    @Column(nullable = true)
    private boolean invoiced = false;
    @Column(nullable = true)
    private LocalDateTime invoicedAt;

    @ManyToOne
    @JoinColumn(name = "plant_id")
    private Plant plant;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "atix_client_id", nullable = false)
    private Client AtixClient;

    @OneToOne(cascade = CascadeType.ALL)
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
    private String NasSubDirectory;

    private int ExpectedOfficeHOurs;

    private int ExpectedPlantHours;

    @OneToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    public Work() {}

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
                 int expectedOfficeHOurs,
                 int expectedPlantHours) {
        Name = name;
        this.bidNumber = bidNumber;
        this.seller = seller;
        OrderNumber = orderNumber;
        OrderDate = orderDate;
        ElectricalSchemaProgression = electricalSchemaProgression;
        ProgrammingProgression = programmingProgression;
        this.expectedStartDate = expectedStartDate;
        this.completed = completed;
        this.completedAt = completedAt;
        this.invoiced = invoiced;
        this.invoicedAt = invoicedAt;
        this.plant = plant;
        AtixClient = atixClient;
        this.finalClient = finalClient;
        NasSubDirectory = nasSubDirectory;
        ExpectedOfficeHOurs = expectedOfficeHOurs;
        ExpectedPlantHours = expectedPlantHours;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
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
        return OrderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        OrderNumber = orderNumber;
    }

    public LocalDate getOrderDate() {
        return OrderDate;
    }
    public void setOrderDate(LocalDate orderDate) {
        OrderDate = orderDate;
    }

    public int getElectricalSchemaProgression() {
        return ElectricalSchemaProgression;
    }
    public void setElectricalSchemaProgression(int electricalSchemaProgression) {
        ElectricalSchemaProgression = electricalSchemaProgression;
    }

    public int getProgrammingProgression() {
        return ProgrammingProgression;
    }
    public void setProgrammingProgression(int programmingProgression) {
        ProgrammingProgression = programmingProgression;
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
        return AtixClient;
    }
    public void setAtixClient(Client atixClient) {
        AtixClient = atixClient;
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
        return NasSubDirectory;
    }
    public void setNasSubDirectory(String nasSubDirectory) {
        NasSubDirectory = nasSubDirectory;
    }

    public int getExpectedOfficeHOurs() {
        return ExpectedOfficeHOurs;
    }
    public void setExpectedOfficeHOurs(int expectedOfficeHOurs) {
        ExpectedOfficeHOurs = expectedOfficeHOurs;
    }

    public int getExpectedPlantHours() {
        return ExpectedPlantHours;
    }
    public void setExpectedPlantHours(int expectedPlantHours) {
        ExpectedPlantHours = expectedPlantHours;
    }

    public Ticket getTicket() {
        return ticket;
    }
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}
