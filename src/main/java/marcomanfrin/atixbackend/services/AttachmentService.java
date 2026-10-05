package marcomanfrin.atixbackend.services;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.ByteArrayInputStream;
import java.util.Map;
import jakarta.transaction.Transactional;
import marcomanfrin.atixbackend.ServiceInterfaces.IAttachmentService;
import marcomanfrin.atixbackend.entities.Attachment;
import marcomanfrin.atixbackend.entities.AttachmentLink;
import marcomanfrin.atixbackend.enums.AttachmentTargetType;
import marcomanfrin.atixbackend.enums.AttachmentType;
import marcomanfrin.atixbackend.exceptions.ForbiddenException;
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
    private final RapportinoRepository rapportinoRepository;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             AttachmentLinkRepository attachmentLinkRepository,
                             MinioClient minioClient,
                             WorkRepository workRepository,
                             PlantRepository plantRepository,
                             TicketRepository ticketRepository,
                             RapportinoRepository rapportinoRepository) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentLinkRepository = attachmentLinkRepository;
        this.minioClient = minioClient;
        this.workRepository = workRepository;
        this.plantRepository = plantRepository;
        this.ticketRepository = ticketRepository;
        this.rapportinoRepository = rapportinoRepository;
    }

    @Override
    @Transactional
    public AttachmentLink uploadAndLink(MultipartFile file, AttachmentTargetType targetType, UUID targetId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        assertUserManaged(targetType);
        assertTargetExists(targetType, targetId);

        try {
            String objectKey = "attachments/" + targetType.name().toLowerCase()
                    + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            String originalFilename = file.getOriginalFilename();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .headers(Map.of("Content-Disposition",
                            "inline; filename=\"" + originalFilename + "\""))
                    .build());

            String url = minioPublicUrl + "/" + bucket + "/" + objectKey;
            String resourceTypeOut = file.getContentType();

            AttachmentType type = determineAttachmentType(file.getContentType());

            Attachment attachment = new Attachment();
            attachment.setUrl(url);
            attachment.setPublicId(objectKey);
            attachment.setOriginalFilename(originalFilename);
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
        assertUserManaged(targetType);
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

        boolean linkedToReport = attachment.getLinks().stream()
                .anyMatch(link -> link.getTargetType() == AttachmentTargetType.REPORT);
        if (linkedToReport) {
            throw new ForbiddenException("Rapportino documents cannot be deleted");
        }

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

    // I documenti dei rapportini (REPORT) sono generati e serviti solo dal backend:
    // gli endpoint generici degli allegati non possono elencarli, aggiungerne o cancellarli.
    private void assertUserManaged(AttachmentTargetType targetType) {
        if (targetType == AttachmentTargetType.REPORT) {
            throw new ForbiddenException("Rapportino documents are managed by the rapportini endpoints");
        }
    }

    /**
     * Salva un file generato dal server sotto un prefisso privato (non coperto dalla policy anonima)
     * e lo collega al target. L'url salvato e' un riferimento interno, non un link scaricabile.
     */
    @Override
    @Transactional
    public AttachmentLink storeGenerated(byte[] content, String filename, String contentType,
                                         String keyPrefix, AttachmentTargetType targetType, UUID targetId) {
        assertTargetExists(targetType, targetId);
        String objectKey = keyPrefix + "/" + targetId + "/" + UUID.randomUUID() + "_" + filename;
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error storing generated file", e);
        }

        Attachment attachment = new Attachment();
        attachment.setUrl("minio://" + bucket + "/" + objectKey);
        attachment.setPublicId(objectKey);
        attachment.setOriginalFilename(filename);
        attachment.setResourceType(contentType);
        attachment.setType(determineAttachmentType(contentType));
        attachmentRepository.save(attachment);

        AttachmentLink link = new AttachmentLink();
        link.setAttachment(attachment);
        link.setTargetType(targetType);
        link.setTargetId(targetId);
        return attachmentLinkRepository.save(link);
    }

    @Override
    public byte[] readContent(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment not found: " + attachmentId));
        try (GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(attachment.getPublicId())
                .build())) {
            return object.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error reading file from storage", e);
        }
    }

    // Rimozione best-effort dell'oggetto, usata per ripulire dopo un rollback
    @Override
    public void removeObjectQuietly(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception ignored) {
            // un oggetto orfano in storage non deve mascherare l'errore originale
        }
    }

    private void assertTargetExists(AttachmentTargetType targetType, UUID targetId) {
        boolean exists = switch (targetType) {
            case WORK -> workRepository.existsById(targetId);
            case PLANT -> plantRepository.existsById(targetId);
            case TICKET -> ticketRepository.existsById(targetId);
            case REPORT -> rapportinoRepository.existsById(targetId);
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
