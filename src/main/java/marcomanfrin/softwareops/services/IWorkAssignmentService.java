package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.entities.WorkAssignment;
import marcomanfrin.softwareops.enums.AssignmentRole;

import java.util.List;
import java.util.UUID;

public interface IWorkAssignmentService {
    void assignTechnicianToWork(UUID workId, UUID technicianId, AssignmentRole role);
    List<WorkAssignment> getAssignmentsByWorkId(UUID workId);
    List<WorkAssignment> getAssignmentsByTechnicianId(UUID technicianId);
}