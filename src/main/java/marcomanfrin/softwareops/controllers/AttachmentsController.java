package marcomanfrin.softwareops.controllers;

import marcomanfrin.softwareops.entities.Attachment;
import marcomanfrin.softwareops.entities.AttachmentLink;
import marcomanfrin.softwareops.enums.AttachmentTargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attachments")
public class AttachmentsController {

    @Autowired
    private IAttachmentService attachmentService;

    @PostMapping("/{targetType}/{targetId}")
    public AttachmentLink uploadAndLinkAttachment(
            @PathVariable AttachmentTargetType targetType,
            @PathVariable UUID targetId,
            @RequestParam("file") MultipartFile file) {
        return attachmentService.upload(file, targetType, targetId);
    }

    @GetMapping("/{targetType}/{targetId}")
    public List<Attachment> getAttachments(
            @PathVariable AttachmentTargetType targetType,
            @PathVariable UUID targetId) {
        return attachmentService.get(targetType, targetId);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID attachmentId) {
        attachmentService.delete(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
