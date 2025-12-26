package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.works.WorkDetailResponse;
import marcomanfrin.atixbackend.DTO.works.WorkRequest;
import marcomanfrin.atixbackend.DTO.works.WorkSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IWorkService {
    WorkDetailResponse createWork(WorkRequest request);
    Page<WorkSummaryResponse> getAllWorks(Pageable pageable);
    WorkDetailResponse getWorkById(UUID id);
    WorkDetailResponse updateWork(UUID id, WorkRequest request);
    void closeWork(UUID id);
    void invoiceWork(UUID id);
    void assignTechnician(UUID workId, UUID technicianId);
    void addWorksiteReference(UUID workId, UUID worksiteReferenceId);
}
