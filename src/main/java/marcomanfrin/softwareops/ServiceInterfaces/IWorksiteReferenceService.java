package marcomanfrin.softwareops.ServiceInterfaces;

import marcomanfrin.softwareops.DTO.worksiteReferences.WorksiteReferenceRequest;
import marcomanfrin.softwareops.DTO.worksiteReferences.WorksiteReferenceResponse;

import java.util.List;
import java.util.UUID;

public interface IWorksiteReferenceService {
    WorksiteReferenceResponse createWorksiteReference(WorksiteReferenceRequest request);
    List<WorksiteReferenceResponse> getAllWorksiteReferences();
    WorksiteReferenceResponse getWorksiteReferenceById(UUID id);
    WorksiteReferenceResponse updateWorksiteReference(UUID id, WorksiteReferenceRequest request);
    void deleteWorksiteReference(UUID id);
}
