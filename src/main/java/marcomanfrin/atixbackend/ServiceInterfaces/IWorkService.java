package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.works.WorkDetailResponse;
import marcomanfrin.atixbackend.DTO.works.WorkRequest;
import marcomanfrin.atixbackend.DTO.works.WorkSummaryResponse;
import marcomanfrin.atixbackend.DTO.works.WorkUpdateRequest;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.WorkStatus;
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IWorkService {
    WorkDetailResponse createWork(WorkRequest request);
    Page<WorkSummaryResponse> getAllWorks(Pageable pageable);
    Page<WorkSummaryResponse> getFilteredWorks(
            UUID clientId,
            UUID atixClientId,
            UUID finalClientId,
            UUID sellerId,
            UUID plantId,
            UUID ticketId,
            UUID technicianId,
            WorkStatus status,
            List<WorkStatus> statuses,
            LocalDate orderDateFrom,
            LocalDate orderDateTo,
            LocalDate expectedStartDateFrom,
            LocalDate expectedStartDateTo,
            String name,
            String bidNumber,
            String orderNumber,
            Pageable pageable
    );
    WorkDetailResponse getWorkById(UUID id);
    WorkDetailResponse updateWork(UUID id, WorkUpdateRequest request);

    void startWork(UUID id, UUID userId, UserRole userRole);
    void closeWork(UUID id, UUID userId, UserRole userRole);
    void invoiceWork(UUID id, UUID userId, UserRole userRole);
    void reopenWork(UUID id, UUID userId, UserRole userRole);
    void forceStatusChange(UUID id, WorkStatus newStatus, UUID userId, UserRole userRole);

    void assignTechnician(UUID workId, UUID technicianId);
    void addWorksiteReference(UUID workId, UUID worksiteReferenceId, WorksiteReferenceRole role);

    void deleteWork(UUID id);
    void removeWorksiteReference(UUID workId, UUID worksiteReferenceId);
    void removeTechnician(UUID workId, UUID technicianId);
}
