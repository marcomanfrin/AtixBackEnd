package marcomanfrin.atixbackend.services;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.transaction.Transactional;
import marcomanfrin.atixbackend.ServiceInterfaces.IAttachmentService;
import marcomanfrin.atixbackend.entities.Attachment;
import marcomanfrin.atixbackend.entities.AttachmentLink;
import marcomanfrin.atixbackend.enums.AttachmentTargetType;
import marcomanfrin.atixbackend.enums.AttachmentType;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import marcomanfrin.atixbackend.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService implements IAttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentLinkRepository attachmentLinkRepository;
    private final MinioClient minioClient;

    @Value("${minio.public-url}")
    private String minioPublicUrl;

    @Value("${minio.bucket}")
    private String bucket;

    private final WorkRepository workRepository;
    private final PlantRepository plantRepository;
    private final TicketRepository ticketRepository;
    private final WorkReportRepository workReportRepository;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             AttachmentLinkRepository attachmentLinkRepository,
                             MinioClient minioClient,
                             WorkRepository workRepository,
                             PlantRepository plantRepository,
                             TicketRepository ticketRepository,
                             WorkReportRepository workReportRepository) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentLinkRepository = attachmentLinkRepository;
        this.minioClient = minioClient;
        this.workRepository = workRepository;
        this.plantRepository = plantRepository;
        this.ticketRepository = ticketRepository;
        this.workReportRepository = workReportRepository;
    }

    @Override
    @Transactional
    public AttachmentLink uploadAndLink(MultipartFile file, AttachmentTargetType targetType, UUID targetId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        assertTargetExists(targetType, targetId);

        try {
            String objectKey = "attachments/" + targetType.name().toLowerCase()
                    + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String url = minioPublicUrl + "/" + bucket + "/" + objectKey;
            String publicId = objectKey;
            String resourceTypeOut = file.getContentType();

            AttachmentType type = determineAttachmentType(file.getContentType());

            Attachment attachment = new Attachment();
            attachment.setUrl(url);
            attachment.setPublicId(publicId);
            attachment.setResourceType(resourceTypeOut);
            attachment.setType(type);
            attachmentRepository.save(attachment);

            AttachmentLink link = new AttachmentLink();
            link.setAttachment(attachment);
            link.setTargetType(targetType);
            link.setTargetId(targetId);
            return attachmentLinkRepository.save(link);

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }

    @Override
    public List<Attachment> getAttachments(AttachmentTargetType targetType, UUID targetId) {
        assertTargetExists(targetType, targetId);

        List<AttachmentLink> links = attachmentLinkRepository.findByTargetTypeAndTargetId(targetType, targetId);

        return links.stream()
                .map(AttachmentLink::getAttachment)
                .distinct()
                .toList();
    }

    @Override
    public void unlink(UUID linkId) {
        if (!attachmentLinkRepository.existsById(linkId)) {
            throw new NotFoundException("AttachmentLink not found: " + linkId);
        }
        attachmentLinkRepository.deleteById(linkId);
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment not found: " + attachmentId));

        if (attachment.getPublicId() != null && !attachment.getPublicId().isBlank()) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(attachment.getPublicId())
                        .build());
            } catch (Exception e) {
                throw new RuntimeException("Error deleting file from MinIO", e);
            }
        }

        // I link vengono eliminati automaticamente grazie al cascade
        attachmentRepository.delete(attachment);
    }

    private void assertTargetExists(AttachmentTargetType targetType, UUID targetId) {
        boolean exists = switch (targetType) {
            case WORK -> workRepository.existsById(targetId);
            case PLANT -> plantRepository.existsById(targetId);
            case TICKET -> ticketRepository.existsById(targetId);
            case REPORT -> workReportRepository.existsById(targetId);
        };

        if (!exists) {
            throw new NotFoundException("Target not found: " + targetType + " " + targetId);
        }
    }

    private AttachmentType determineAttachmentType(String contentType) {
        if (contentType == null) {
            return AttachmentType.OTHER;
        }
        if (contentType.startsWith("image/")) {
            return AttachmentType.PHOTO;
        }
        if (contentType.equals("application/pdf")) {
            return AttachmentType.PDF;
        }
        if (contentType.startsWith("application/vnd.openxmlformats-officedocument") || contentType.equals("application/msword")) {
            return AttachmentType.DOC;
        }
        return AttachmentType.OTHER;
    }
}
