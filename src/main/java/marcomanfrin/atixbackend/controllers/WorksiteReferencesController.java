package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceRequest;
import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceResponse;
import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceUpdateRequest;
import marcomanfrin.atixbackend.services.WorksiteReferenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/worksite-references")
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'USER')")
public class WorksiteReferencesController {
    private final WorksiteReferenceService worksiteReferenceService;

    public WorksiteReferencesController(WorksiteReferenceService worksiteReferenceService) {
        this.worksiteReferenceService = worksiteReferenceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<WorksiteReferenceResponse> createWorksiteReference(
            @Valid @RequestBody WorksiteReferenceRequest request) {
        WorksiteReferenceResponse worksiteReference = worksiteReferenceService.createWorksiteReference(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(worksiteReference);
    }

    @GetMapping
    public ResponseEntity<List<WorksiteReferenceResponse>> getAllWorksiteReferences() {
        List<WorksiteReferenceResponse> worksiteReferences = worksiteReferenceService.getAllWorksiteReferences();
        return ResponseEntity.ok(worksiteReferences);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorksiteReferenceResponse> getWorksiteReferenceById(@PathVariable UUID id) {
        WorksiteReferenceResponse worksiteReference = worksiteReferenceService.getWorksiteReferenceById(id);
        return ResponseEntity.ok(worksiteReference);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<WorksiteReferenceResponse> updateWorksiteReference(
            @PathVariable UUID id,
            @Valid @RequestBody WorksiteReferenceUpdateRequest request) {
        WorksiteReferenceResponse worksiteReference = worksiteReferenceService.updateWorksiteReference(id, request);
        return ResponseEntity.ok(worksiteReference);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorksiteReference(@PathVariable UUID id) {
        worksiteReferenceService.deleteWorksiteReference(id);
        return ResponseEntity.noContent().build();
    }
}
