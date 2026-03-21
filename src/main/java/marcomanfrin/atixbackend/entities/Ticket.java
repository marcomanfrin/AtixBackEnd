package marcomanfrin.atixbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import marcomanfrin.atixbackend.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue
    private UUID id;

    private String senderEmail;

    @OneToOne
    @JoinColumn(name = "order_number_id")
    private Work orderNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime statusChangedAt;

    @Column(name = "status_changed_by")
    private UUID statusChangedBy;

    public Ticket() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Ticket(String senderEmail, Work orderNumber, String name, String description, TicketStatus status) {
        this.senderEmail = senderEmail;
        this.orderNumber = orderNumber;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    // Status helper methods
    public boolean isOpen() {
        return this.status == TicketStatus.OPEN;
    }

    public boolean isInProgress() {
        return this.status == TicketStatus.IN_PROGRESS;
    }

    public boolean isResolved() {
        return this.status == TicketStatus.RESOLVED;
    }

    public boolean isClosed() {
        return this.status == TicketStatus.CLOSED;
    }

    public TicketStatus getStatus() {
        return status;
    }
    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Work getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(Work orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getSenderEmail() {
        return senderEmail;
    }
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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
}