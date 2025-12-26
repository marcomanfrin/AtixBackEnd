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

    @Column(nullable = false)
    private String notes;

    @Column(nullable = false)
    private String nasDirectory;

    @Column(nullable = false)
    private String pswPhrase;

    @Column(nullable = false)
    private String pswPlatform;

    @Column(nullable = false)
    private String pswStation;

    public Plant() {
    }

    public Plant(String name,
                 String notes,
                 String nasDirectory,
                 String pswPhrase,
                 String pswPlatform,
                 String pswStation) {
        this.name = name;
        this.notes = notes;
        this.nasDirectory = nasDirectory;
        this.pswPhrase = pswPhrase;
        this.pswPlatform = pswPlatform;
        this.pswStation = pswStation;
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

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNasDirectory() {
        return nasDirectory;
    }
    public void setNasDirectory(String nasDirectory) {
        this.nasDirectory = nasDirectory;
    }

    public String getPswPhrase() {
        return pswPhrase;
    }
    public void setPswPhrase(String pswPhrase) {
        this.pswPhrase = pswPhrase;
    }

    public String getPswPlatform() {
        return pswPlatform;
    }
    public void setPswPlatform(String pswPlatform) {
        this.pswPlatform = pswPlatform;
    }

    public String getPswStation() {
        return pswStation;
    }
    public void setPswStation(String pswStation) {
        this.pswStation = pswStation;
    }
}