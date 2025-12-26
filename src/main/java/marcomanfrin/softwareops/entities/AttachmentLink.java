package marcomanfrin.softwareops.entities;

import jakarta.persistence.*;
import marcomanfrin.softwareops.enums.AttachmentTargetType;

import java.util.UUID;

@Entity
@Table(
        name = "attachment_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attachment_target",
                        columnNames = {"attachment_id", "target_type", "target_id"}
                )
        }
)
public class AttachmentLink {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false)
    private Attachment attachment;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private AttachmentTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    public AttachmentLink() {}

    public AttachmentLink(Attachment attachment, AttachmentTargetType targetType, UUID targetId) {
        this.attachment = attachment;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public UUID getId() {
        return id;
    }

    public Attachment getAttachment() {
        return attachment;
    }
    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public AttachmentTargetType getTargetType() {
        return targetType;
    }
    public void setTargetType(AttachmentTargetType targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }
    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }
}
