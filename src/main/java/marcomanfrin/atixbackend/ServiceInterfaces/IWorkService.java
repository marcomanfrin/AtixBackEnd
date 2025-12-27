package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.works.WorkDetailResponse;
import marcomanfrin.atixbackend.DTO.works.WorkRequest;
import marcomanfrin.atixbackend.DTO.works.WorkSummaryResponse;
import marcomanfrin.atixbackend.DTO.works.WorkUpdateRequest;
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
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
            Boolean completed,
            Boolean invoiced,
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
    void closeWork(UUID id);
    void invoiceWork(UUID id);
    void assignTechnician(UUID workId, UUID technicianId);
    void addWorksiteReference(UUID workId, UUID worksiteReferenceId, WorksiteReferenceRole role);
}
