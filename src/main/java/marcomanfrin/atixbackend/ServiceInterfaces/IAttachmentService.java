package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.entities.Attachment;
import marcomanfrin.atixbackend.entities.AttachmentLink;
import marcomanfrin.atixbackend.enums.AttachmentTargetType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IAttachmentService {
    AttachmentLink uploadAndLink(MultipartFile file, AttachmentTargetType targetType, UUID targetId);
    List<Attachment> getAttachments(AttachmentTargetType targetType, UUID targetId);
    void unlink(UUID linkId);
    void deleteAttachment(UUID attachmentId);
}
