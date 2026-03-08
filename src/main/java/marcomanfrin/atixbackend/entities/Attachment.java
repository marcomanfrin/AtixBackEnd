package marcomanfrin.atixbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import marcomanfrin.atixbackend.enums.AttachmentType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String url;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentType type;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @OneToMany(mappedBy = "attachment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AttachmentLink> links = new ArrayList<>();

    public Attachment() {}

    public Attachment(String url, String publicId, String resourceType, AttachmentType type) {
        this.url = url;
        this.publicId = publicId;
        this.resourceType = resourceType;
        this.type = type;
    }

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public AttachmentType getType() {
        return type;
    }
    public void setType(AttachmentType type) {
        this.type = type;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public List<AttachmentLink> getLinks() {
        return links;
    }
    public void setLinks(List<AttachmentLink> links) {
        this.links = links;
    }
}