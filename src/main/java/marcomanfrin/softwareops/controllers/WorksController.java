package marcomanfrin.softwareops.controllers;

import jakarta.validation.Valid;
import marcomanfrin.softwareops.DTO.works.AssignTechnicianRequest;
import marcomanfrin.softwareops.DTO.works.UpdateWorkRequest;
import marcomanfrin.softwareops.DTO.works.WorkResponseDTO;
import marcomanfrin.softwareops.entities.Work;
import marcomanfrin.softwareops.enums.WorkStatus;
import marcomanfrin.softwareops.services.IWorkAssignmentService;
import marcomanfrin.softwareops.services.IWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/works")
public class WorksController {

    private final IWorkService workService;
    private final IWorkAssignmentService workAssignmentService;

    public WorksController(IWorkService workService, IWorkAssignmentService workAssignmentService) {
        this.workService = workService;
        this.workAssignmentService = workAssignmentService;
    }

    @GetMapping
    public Page<WorkResponseDTO> getWorks(
            @RequestParam(required = false) WorkStatus status,
            @RequestParam(required = false) UUID technicianId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return workService.getWorks(status, technicianId, pageable)
                .map(WorkResponseDTO::fromEntity);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','TECHNICIAN')")
    public WorkResponseDTO getWorkById(@PathVariable UUID id) {
        return WorkResponseDTO.fromEntity(workService.getWorkByIdOrThrow(id));
    }

    // Update SOLO status: PATCH semantico
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public WorkResponseDTO updateWorkStatus(@PathVariable UUID id, @RequestBody @Valid UpdateWorkRequest request) {
        return WorkResponseDTO.fromEntity(workService.updateWorkStatus(id, request.status()));
    }

    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ResponseEntity<Void> assignTechnician(@PathVariable UUID id, @RequestBody @Valid AssignTechnicianRequest request) {
        workAssignmentService.assignTechnicianToWork(id, request.technicianId(), request.role());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','TECHNICIAN')")
    public WorkResponseDTO completeWork(@PathVariable UUID id) {
        return WorkResponseDTO.fromEntity(workService.completeWork(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public ResponseEntity<Void> deleteWork(@PathVariable UUID id) {
        workService.deleteWork(id);
        return ResponseEntity.noContent().build();
    }
}
