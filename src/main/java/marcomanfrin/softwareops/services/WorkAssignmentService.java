package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.entities.TechnicianUser;
import marcomanfrin.softwareops.entities.User;
import marcomanfrin.softwareops.entities.Work;
import marcomanfrin.softwareops.entities.WorkAssignment;
import marcomanfrin.softwareops.enums.AssignmentRole;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.UserRepository;
import marcomanfrin.softwareops.repositories.WorkAssignmentRepository;
import marcomanfrin.softwareops.repositories.WorkRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkAssignmentService implements IWorkAssignmentService {

    private final WorkAssignmentRepository workAssignmentRepository;
    private final WorkRepository workRepository;
    private final UserRepository userRepository;

    public WorkAssignmentService(WorkAssignmentRepository workAssignmentRepository, WorkRepository workRepository, UserRepository userRepository) {
        this.workAssignmentRepository = workAssignmentRepository;
        this.workRepository = workRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void assignTechnicianToWork(UUID workId, UUID technicianId, AssignmentRole role) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found: " + workId));

        User user = userRepository.findById(technicianId)
                .orElseThrow(() -> new NotFoundException("User not found: " + technicianId));

        boolean isTechnician = (user instanceof TechnicianUser);
        if (!isTechnician) {
            throw new IllegalArgumentException("User is not a technician: " + technicianId);
        }

        if (workAssignmentRepository.existsByWork_IdAndUser_Id(workId, technicianId)) {
            WorkAssignment existing = workAssignmentRepository.findByWork_IdAndUser_Id(workId, technicianId).orElseThrow();
            existing.setAssignmentRole(role);
            workAssignmentRepository.save(existing);
            return;
        }

        WorkAssignment assignment = new WorkAssignment();
        assignment.setWork(work);
        assignment.setUser(user);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setAssignmentRole(role);

        workAssignmentRepository.save(assignment);
    }

    @Override
    public List<WorkAssignment> getAssignmentsByWorkId(UUID workId) {
        return workAssignmentRepository.findByWork_Id(workId);
    }

    @Override
    public List<WorkAssignment> getAssignmentsByTechnicianId(UUID technicianId) {
        return workAssignmentRepository.findByUser_Id(technicianId);
    }
}
