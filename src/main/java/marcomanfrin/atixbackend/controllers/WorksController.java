package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.works.AddWorksiteReferenceRequest;
import marcomanfrin.atixbackend.DTO.works.AssignTechnicianRequest;
import marcomanfrin.atixbackend.DTO.works.WorkDetailResponse;
import marcomanfrin.atixbackend.DTO.works.WorkRequest;
import marcomanfrin.atixbackend.DTO.works.WorkSummaryResponse;
import marcomanfrin.atixbackend.DTO.works.WorkUpdateRequest;
import marcomanfrin.atixbackend.services.WorkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/works")
public class WorksController {
    private final WorkService workService;

    public WorksController(WorkService workService) {
        this.workService = workService;
    }

    @PostMapping
    public ResponseEntity<WorkDetailResponse> createWork(@Valid @RequestBody WorkRequest request) {
        WorkDetailResponse work = workService.createWork(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(work);
    }

    @GetMapping
    public ResponseEntity<Page<WorkSummaryResponse>> getAllWorks(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID atixClientId,
            @RequestParam(required = false) UUID finalClientId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) UUID plantId,
            @RequestParam(required = false) UUID ticketId,
            @RequestParam(required = false) UUID technicianId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Boolean invoiced,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedStartDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedStartDateTo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bidNumber,
            @RequestParam(required = false) String orderNumber,
            Pageable pageable) {

        // If any filter is provided, use filtered search
        if (clientId != null || atixClientId != null || finalClientId != null ||
            sellerId != null || plantId != null || ticketId != null || technicianId != null ||
            completed != null || invoiced != null ||
            orderDateFrom != null || orderDateTo != null ||
            expectedStartDateFrom != null || expectedStartDateTo != null ||
            name != null || bidNumber != null || orderNumber != null) {

            Page<WorkSummaryResponse> works = workService.getFilteredWorks(
                    clientId, atixClientId, finalClientId, sellerId, plantId,
                    ticketId, technicianId, completed, invoiced,
                    orderDateFrom, orderDateTo, expectedStartDateFrom, expectedStartDateTo,
                    name, bidNumber, orderNumber, pageable
            );
            return ResponseEntity.ok(works);
        }

        // Otherwise, return all works
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
            @Valid @RequestBody WorkUpdateRequest request) {
        WorkDetailResponse work = workService.updateWork(id, request);
        return ResponseEntity.ok(work);
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("@securityService.isTechnician(authentication)")
    public ResponseEntity<Void> closeWork(@PathVariable UUID id) {
        workService.closeWork(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/invoice")
    @PreAuthorize("@securityService.isAdministrative(authentication)")
    public ResponseEntity<Void> invoiceWork(@PathVariable UUID id) {
        workService.invoiceWork(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign-technician")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or @securityService.isTechnician(authentication)")
    public ResponseEntity<Void> assignTechnician(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTechnicianRequest request) {
        workService.assignTechnician(id, request.technicianId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}/add-reference")
    public ResponseEntity<Void> addWorksiteReference(
            @PathVariable UUID id,
            @Valid @RequestBody AddWorksiteReferenceRequest request) {
        workService.addWorksiteReference(id, request.worksiteReferenceId(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
