package marcomanfrin.atixbackend.controllers;

import marcomanfrin.atixbackend.ServiceInterfaces.IAttachmentService;
import marcomanfrin.atixbackend.entities.Attachment;
import marcomanfrin.atixbackend.entities.AttachmentLink;
import marcomanfrin.atixbackend.enums.AttachmentTargetType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attachments")
public class AttachmentsController {

    private final IAttachmentService attachmentService;

    public AttachmentsController(IAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/{targetType}/{targetId}")
    public ResponseEntity<AttachmentLink> uploadAndLinkAttachment(
            @PathVariable AttachmentTargetType targetType,
            @PathVariable UUID targetId,
            @RequestParam("file") MultipartFile file) {
        AttachmentLink link = attachmentService.uploadAndLink(file, targetType, targetId);
        return ResponseEntity.status(HttpStatus.CREATED).body(link);
    }

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<List<Attachment>> getAttachments(
            @PathVariable AttachmentTargetType targetType,
            @PathVariable UUID targetId) {
        List<Attachment> attachments = attachmentService.getAttachments(targetType, targetId);
        return ResponseEntity.ok(attachments);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
