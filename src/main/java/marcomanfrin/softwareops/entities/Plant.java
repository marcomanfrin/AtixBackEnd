package marcomanfrin.softwareops.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import marcomanfrin.softwareops.DTO.plants.PlantResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plants")
public class Plant {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false, name = "order_number")
    private String orderNumber;

    @Column(nullable = false)
    private boolean invoiced;

    private LocalDateTime invoicedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_client_id")
    private Client primaryClient;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "final_client_id")
    private Client finalClient;

    @OneToMany(mappedBy = "plant")
    @JsonIgnore
    private List<WorkFromPlant> worksFromPlant = new ArrayList<>();

    public List<WorkFromPlant> getWorksFromPlant() { return worksFromPlant; }

    public void addWorkFromPlant(WorkFromPlant work) {
        worksFromPlant.add(work);
        work.setPlant(this);
    }

    public void removeWorkFromPlant(WorkFromPlant work) {
        worksFromPlant.remove(work);
        work.setPlant(null);
    }

    public Plant() {}

    public Plant(String name, String notes, String orderNumber) {
        this.name = name;
        this.notes = notes;
        this.orderNumber = orderNumber;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public boolean getInvoiced() {
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

    public Client getPrimaryClient() {
        return primaryClient;
    }
    public void setPrimaryClient(Client primaryClient) {
        this.primaryClient = primaryClient;
    }

    public Client getFinalClient() {
        return finalClient;
    }
    public void setFinalClient(Client finalClient) {
        this.finalClient = finalClient;
    }
}