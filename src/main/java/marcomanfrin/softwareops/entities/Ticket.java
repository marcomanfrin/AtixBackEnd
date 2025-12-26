package marcomanfrin.softwareops.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import marcomanfrin.softwareops.enums.TicketStatus;

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
    private Work OrderNumber;

    @Column(nullable = false)
    private String Name;

    @Column(nullable = false)
    private String Description;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    public Ticket() {}

    public Ticket(String senderEmail, Work orderNumber, String name, String description, TicketStatus status) {
        this.senderEmail = senderEmail;
        OrderNumber = orderNumber;
        Name = name;
        Description = description;
        this.status = status;
    }

    public TicketStatus getStatus() {
        return status;
    }
    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        Description = description;
    }

    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }

    public Work getOrderNumber() {
        return OrderNumber;
    }
    public void setOrderNumber(Work orderNumber) {
        OrderNumber = orderNumber;
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
}