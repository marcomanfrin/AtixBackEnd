package marcomanfrin.softwareops.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import marcomanfrin.softwareops.entities.Attachment;
import marcomanfrin.softwareops.entities.AttachmentLink;
import marcomanfrin.softwareops.enums.AttachmentTargetType;
import marcomanfrin.softwareops.enums.AttachmentType;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class AttachmentService implements IAttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentLinkRepository attachmentLinkRepository;
    private final Cloudinary cloudinary;

    private final WorkRepository workRepository;
    private final PlantRepository plantRepository;
    private final TicketRepository ticketRepository;
    private final WorkReportRepository workReportRepository;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             AttachmentLinkRepository attachmentLinkRepository,
                             Cloudinary cloudinary, WorkRepository workRepository,
                             PlantRepository plantRepository,
                             TicketRepository ticketRepository,
                             WorkReportRepository workReportRepository) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentLinkRepository = attachmentLinkRepository;
        this.cloudinary = cloudinary;
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

        try
        {
            String resourceTypeIn = Objects.requireNonNull(file.getContentType()).startsWith("image/")
                    ? "image"
                    : "raw";

            var options = ObjectUtils.asMap(
                    "folder", "SoftwareOps/attachments" + targetType.name().toLowerCase(),
                    "resource_type", resourceTypeIn
            );

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            String url = result.get("secure_url").toString();
            String publicId = result.get("public_id").toString();
            String resourceTypeOut = result.get("resource_type").toString();

            AttachmentType type = determineAttachmentType(file.getContentType());

            Attachment attachment = new Attachment();
            attachment.setUrl(url);
            attachment.setPublicId(publicId);
            attachment.setResourceType(resourceTypeOut);
            attachment.setType(type);
            attachment.setUploadedAt(LocalDateTime.now());
            attachmentRepository.save(attachment);

            AttachmentLink link = new AttachmentLink();
            link.setAttachment(attachment);
            link.setTargetType(targetType);
            link.setTargetId(targetId);
            return attachmentLinkRepository.save(link);

        } catch (IOException e) {
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
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + attachmentId));

        if (attachment.getPublicId() != null && !attachment.getPublicId().isBlank()) {
            try {
                String rt = (attachment.getResourceType() == null || attachment.getResourceType().isBlank())
                        ? "auto"
                        : attachment.getResourceType();

                cloudinary.uploader().destroy(
                        attachment.getPublicId(),
                        ObjectUtils.asMap("resource_type", rt)
                );
            } catch (Exception e) {
                throw new RuntimeException("Error deleting file from Cloudinary", e);
            }
        }

        // elimina prima i link
        List<AttachmentLink> links = attachmentLinkRepository.findByAttachment_Id(attachmentId);
        attachmentLinkRepository.deleteAll(links);

        // poi l’attachment
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

    private String requireNotBlank(String value, String field) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value.trim();
    }
}
