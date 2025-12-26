package marcomanfrin.softwareops.controllers;

import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.workReports.AddWorkReportEntryRequest;
import marcomanfrin.softwareops.DTO.workReports.UpdateWorkReportEntryRequest;
import marcomanfrin.softwareops.entities.WorkReport;
import marcomanfrin.softwareops.entities.WorkReportEntry;
import marcomanfrin.softwareops.services.IWorkReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/works/{workId}/reports")
public class ReportsController {

    private final IWorkReportService workReportService;

    public ReportsController(IWorkReportService workReportService) {
        this.workReportService = workReportService;
    }

    @PostMapping
    public ResponseEntity<WorkReport> create(@PathVariable UUID workId) {
        WorkReport created = workReportService.createForWork(workId);
        return ResponseEntity
                .created(URI.create("/works/" + workId + "/report"))
                .body(created);
    }

    @GetMapping
    public WorkReport get(@PathVariable UUID workId) {
        return workReportService.getByWorkIdOrThrow(workId);
    }

    @PostMapping("/rows")
    public ResponseEntity<WorkReportEntry> addRow(
            @PathVariable UUID workId,
            @RequestBody @Valid AddWorkReportEntryRequest request
    ) {
        WorkReportEntry entry = workReportService.addEntryByWorkId(workId, request.taskId(), request.hours());
        return ResponseEntity.status(201).body(entry);
    }

    @PatchMapping("/rows/{rowId}")
    public WorkReportEntry updateRow(
            @PathVariable UUID workId,
            @PathVariable UUID rowId,
            @RequestBody @Valid UpdateWorkReportEntryRequest request
    ) {
        return workReportService.updateEntry(workId, rowId, request.hours());
    }

    @DeleteMapping("/rows/{rowId}")
    public ResponseEntity<Void> deleteRow(@PathVariable UUID workId, @PathVariable UUID rowId) {
        workReportService.removeEntry(workId, rowId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/finalize")
    public WorkReport finalize(@PathVariable UUID workId) {
        return workReportService.finalizeByWorkId(workId);
    }
}