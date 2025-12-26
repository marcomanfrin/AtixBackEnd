package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.worksiteReferences.WorksiteReferenceRequest;
import marcomanfrin.softwareops.DTO.worksiteReferences.WorksiteReferenceResponse;
import marcomanfrin.softwareops.ServiceInterfaces.IWorksiteReferenceService;
import marcomanfrin.softwareops.entities.WorksiteReference;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.WorksiteReferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorksiteReferenceService implements IWorksiteReferenceService {
    private final WorksiteReferenceRepository worksiteReferenceRepository;

    public WorksiteReferenceService(WorksiteReferenceRepository worksiteReferenceRepository) {
        this.worksiteReferenceRepository = worksiteReferenceRepository;
    }

    @Override
    @Transactional
    public WorksiteReferenceResponse createWorksiteReference(WorksiteReferenceRequest request) {
        WorksiteReference worksiteReference = new WorksiteReference(request.name());
        WorksiteReference saved = worksiteReferenceRepository.save(worksiteReference);
        return toWorksiteReferenceResponse(saved);
    }

    @Override
    public List<WorksiteReferenceResponse> getAllWorksiteReferences() {
        return worksiteReferenceRepository.findAll().stream()
                .map(this::toWorksiteReferenceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WorksiteReferenceResponse getWorksiteReferenceById(UUID id) {
        WorksiteReference worksiteReference = worksiteReferenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Worksite reference not found with id: " + id));
        return toWorksiteReferenceResponse(worksiteReference);
    }

    @Override
    @Transactional
    public WorksiteReferenceResponse updateWorksiteReference(UUID id, WorksiteReferenceRequest request) {
        WorksiteReference worksiteReference = worksiteReferenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Worksite reference not found with id: " + id));

        // PATCH logic: update only non-null fields
        if (request.name() != null) {
            worksiteReference.setName(request.name());
        }

        WorksiteReference updated = worksiteReferenceRepository.save(worksiteReference);
        return toWorksiteReferenceResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWorksiteReference(UUID id) {
        if (!worksiteReferenceRepository.existsById(id)) {
            throw new NotFoundException("Worksite reference not found with id: " + id);
        }
        worksiteReferenceRepository.deleteById(id);
    }

    private WorksiteReferenceResponse toWorksiteReferenceResponse(WorksiteReference worksiteReference) {
        return new WorksiteReferenceResponse(
                worksiteReference.getId(),
                worksiteReference.getName()
        );
    }
}
