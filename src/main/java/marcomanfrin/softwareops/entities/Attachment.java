package marcomanfrin.softwareops.entities;

import jakarta.persistence.*;
        import marcomanfrin.softwareops.enums.AttachmentType;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    @URL
    private String url;

    @Column(name = "public_id", nullable = false)
    private String publicId; // Cloudinary public_id (to delete content from Cloudinary)

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentType type;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

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
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}