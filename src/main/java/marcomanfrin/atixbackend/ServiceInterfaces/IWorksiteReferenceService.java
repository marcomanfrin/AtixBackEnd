package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceRequest;
import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceResponse;
import marcomanfrin.atixbackend.DTO.worksiteReferences.WorksiteReferenceUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface IWorksiteReferenceService {
    WorksiteReferenceResponse createWorksiteReference(WorksiteReferenceRequest request);
    List<WorksiteReferenceResponse> getAllWorksiteReferences();
    WorksiteReferenceResponse getWorksiteReferenceById(UUID id);
    WorksiteReferenceResponse updateWorksiteReference(UUID id, WorksiteReferenceUpdateRequest request);
    void deleteWorksiteReference(UUID id);
}
