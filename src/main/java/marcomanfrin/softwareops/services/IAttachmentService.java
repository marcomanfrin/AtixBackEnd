package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.entities.Attachment;
import marcomanfrin.softwareops.entities.AttachmentLink;
import marcomanfrin.softwareops.enums.AttachmentTargetType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IAttachmentService {
    AttachmentLink uploadAndLink(MultipartFile file, AttachmentTargetType targetType, UUID targetId);
    List<Attachment> getAttachments(AttachmentTargetType targetType, UUID targetId);
    void unlink(UUID linkId);
    void deleteAttachment(UUID attachmentId);
}
