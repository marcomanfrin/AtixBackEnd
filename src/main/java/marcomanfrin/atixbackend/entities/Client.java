package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;
import marcomanfrin.atixbackend.enums.ClientType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientType type;

    @OneToMany(mappedBy = "atixClient")
    private List<Work> worksAsAtixClient = new ArrayList<>();

    @OneToMany(mappedBy = "finalClient")
    private List<Work> worksAsFinalClient = new ArrayList<>();

    public Client() {}

    public Client(String name, ClientType type) {
        this.name = name;
        this.type = type;
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

    public ClientType getType() {
        return type;
    }
    public void setType(ClientType type) {
        this.type = type;
    }

    public List<Work> getWorksAsAtixClient() {
        return worksAsAtixClient;
    }
    public void setWorksAsAtixClient(List<Work> worksAsAtixClient) {
        this.worksAsAtixClient = worksAsAtixClient;
    }

    public List<Work> getWorksAsFinalClient() {
        return worksAsFinalClient;
    }
    public void setWorksAsFinalClient(List<Work> worksAsFinalClient) {
        this.worksAsFinalClient = worksAsFinalClient;
    }
}