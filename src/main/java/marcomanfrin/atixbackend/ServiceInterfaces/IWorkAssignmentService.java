package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.entities.WorkAssignment;

import java.util.List;
import java.util.UUID;

public interface IWorkAssignmentService {
    List<WorkAssignment> getAssignmentsByWorkId(UUID workId);
    List<WorkAssignment> getAssignmentsByUserId(UUID userId);
    void removeAssignment(UUID workId, UUID userId);
}
