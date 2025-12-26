package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.works.AddWorksiteReferenceRequest;
import marcomanfrin.atixbackend.DTO.works.AssignTechnicianRequest;
import marcomanfrin.atixbackend.DTO.works.WorkDetailResponse;
import marcomanfrin.atixbackend.DTO.works.WorkRequest;
import marcomanfrin.atixbackend.DTO.works.WorkSummaryResponse;
import marcomanfrin.atixbackend.services.WorkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/works")
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'USER')")
public class WorksController {
    private final WorkService workService;

    public WorksController(WorkService workService) {
        this.workService = workService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<WorkDetailResponse> createWork(@Valid @RequestBody WorkRequest request) {
        WorkDetailResponse work = workService.createWork(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(work);
    }

    @GetMapping
    public ResponseEntity<Page<WorkSummaryResponse>> getAllWorks(Pageable pageable) {
        Page<WorkSummaryResponse> works = workService.getAllWorks(pageable);
        return ResponseEntity.ok(works);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkDetailResponse> getWorkById(@PathVariable UUID id) {
        WorkDetailResponse work = workService.getWorkById(id);
        return ResponseEntity.ok(work);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<WorkDetailResponse> updateWork(
            @PathVariable UUID id,
            @Valid @RequestBody WorkRequest request) {
        WorkDetailResponse work = workService.updateWork(id, request);
        return ResponseEntity.ok(work);
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'USER')")
    public ResponseEntity<Void> closeWork(@PathVariable UUID id) {
        workService.closeWork(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Void> invoiceWork(@PathVariable UUID id) {
        workService.invoiceWork(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign-technician")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Void> assignTechnician(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTechnicianRequest request) {
        workService.assignTechnician(id, request.technicianId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}/add-reference")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'USER')")
    public ResponseEntity<Void> addWorksiteReference(
            @PathVariable UUID id,
            @Valid @RequestBody AddWorksiteReferenceRequest request) {
        workService.addWorksiteReference(id, request.worksiteReferenceId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
