package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.ServiceInterfaces.IAttachmentService;
import marcomanfrin.atixbackend.entities.Attachment;
import marcomanfrin.atixbackend.entities.AttachmentLink;
import marcomanfrin.atixbackend.enums.AttachmentTargetType;
import marcomanfrin.atixbackend.enums.AttachmentType;
import marcomanfrin.atixbackend.repositories.AttachmentLinkRepository;
import marcomanfrin.atixbackend.repositories.AttachmentRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Sostituisce MinIO nei test: righe Attachment/AttachmentLink vere, contenuto in memoria
class InMemoryAttachmentService implements IAttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentLinkRepository attachmentLinkRepository;
    final Map<String, byte[]> objects = new ConcurrentHashMap<>();
    final Set<String> removed = ConcurrentHashMap.newKeySet();
    volatile boolean failNextStore;

    InMemoryAttachmentService(AttachmentRepository attachmentRepository, AttachmentLinkRepository attachmentLinkRepository) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentLinkRepository = attachmentLinkRepository;
    }

    @Override
    public AttachmentLink storeGenerated(byte[] content, String filename, String contentType,
                                         String keyPrefix, AttachmentTargetType targetType, UUID targetId) {
        if (failNextStore) {
            failNextStore = false;
            throw new RuntimeException("simulated storage failure");
        }
        String key = keyPrefix + "/" + targetId + "/" + UUID.randomUUID() + "_" + filename;
        objects.put(key, content);
        Attachment attachment = new Attachment("minio://test/" + key, key, contentType, AttachmentType.PDF);
        attachment.setOriginalFilename(filename);
        attachmentRepository.save(attachment);
        return attachmentLinkRepository.save(new AttachmentLink(attachment, targetType, targetId));
    }

    @Override
    public byte[] readContent(UUID attachmentId) {
        return objects.get(attachmentRepository.findById(attachmentId).orElseThrow().getPublicId());
    }

    @Override
    public void removeObjectQuietly(String objectKey) {
        objects.remove(objectKey);
        removed.add(objectKey);
    }

    @Override
    public AttachmentLink uploadAndLink(MultipartFile file, AttachmentTargetType targetType, UUID targetId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Attachment> getAttachments(AttachmentTargetType targetType, UUID targetId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlink(UUID linkId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAttachment(UUID attachmentId) {
        throw new UnsupportedOperationException();
    }
}
