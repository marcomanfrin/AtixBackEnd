package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.AttachmentLink;
import marcomanfrin.softwareops.enums.AttachmentTargetType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentLinkRepository extends JpaRepository<@NonNull AttachmentLink, @NonNull UUID> {
    List<AttachmentLink> findByTargetTypeAndTargetId(AttachmentTargetType targetType, UUID targetId);
    List<AttachmentLink> findByAttachment_Id(UUID attachmentId);
    boolean existsByAttachment_IdAndTargetTypeAndTargetId(UUID attachmentId, AttachmentTargetType targetType, UUID targetId);
}
