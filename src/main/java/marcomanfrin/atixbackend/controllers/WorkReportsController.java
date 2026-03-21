package marcomanfrin.atixbackend.controllers;

import jakarta.validation.Valid;
import marcomanfrin.atixbackend.DTO.workReports.WorkReportEntryRequest;
import marcomanfrin.atixbackend.DTO.workReports.WorkReportEntryResponse;
import marcomanfrin.atixbackend.DTO.workReports.WorkReportEntryUpdateRequest;
import marcomanfrin.atixbackend.DTO.workReports.WorkReportResponse;
import marcomanfrin.atixbackend.services.WorkReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/work-reports")
public class WorkReportsController {
    private final WorkReportService workReportService;

    public WorkReportsController(WorkReportService workReportService) {
        this.workReportService = workReportService;
    }

    // Work Report endpoints
    @GetMapping("/work/{workId}")
    public ResponseEntity<WorkReportResponse> getWorkReportByWorkId(@PathVariable UUID workId) {
        WorkReportResponse workReport = workReportService.getWorkReportByWorkId(workId);
        return ResponseEntity.ok(workReport);
    }

    // Work Report Entry endpoints
    @PostMapping("/entries")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or @securityService.isTechnician(authentication)")
    public ResponseEntity<WorkReportEntryResponse> createWorkReportEntry(
            @Valid @RequestBody WorkReportEntryRequest request) {
        WorkReportEntryResponse entry = workReportService.createWorkReportEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PatchMapping("/entries/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or @securityService.isTechnician(authentication)")
    public ResponseEntity<WorkReportEntryResponse> updateWorkReportEntry(
            @PathVariable UUID id,
            @Valid @RequestBody WorkReportEntryUpdateRequest request) {
        WorkReportEntryResponse entry = workReportService.updateWorkReportEntry(id, request);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/entries/work/{workId}")
    public ResponseEntity<List<WorkReportEntryResponse>> getWorkReportEntriesByWorkId(@PathVariable UUID workId) {
        List<WorkReportEntryResponse> entries = workReportService.getWorkReportEntriesByWorkId(workId);
        return ResponseEntity.ok(entries);
    }

    // B3: allow ADMIN and ADMINISTRATION users (not only OWNER) to delete report entries
    @DeleteMapping("/entries/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or @securityService.isTechnician(authentication)")
    public ResponseEntity<Void> deleteWorkReportEntry(@PathVariable UUID id) {
        workReportService.deleteWorkReportEntry(id);
        return ResponseEntity.noContent().build();
    }
}
