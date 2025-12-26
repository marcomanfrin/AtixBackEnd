package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.ServiceInterfaces.IWorkAssignmentService;
import marcomanfrin.softwareops.entities.Work;
import marcomanfrin.softwareops.entities.WorkAssignment;
import marcomanfrin.softwareops.entities.users.User;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.WorkAssignmentRepository;
import marcomanfrin.softwareops.repositories.WorkRepository;
import marcomanfrin.softwareops.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WorkAssignmentService implements IWorkAssignmentService {
    private final WorkAssignmentRepository workAssignmentRepository;
    private final WorkRepository workRepository;
    private final UserRepository userRepository;

    public WorkAssignmentService(WorkAssignmentRepository workAssignmentRepository,
                                WorkRepository workRepository,
                                UserRepository userRepository) {
        this.workAssignmentRepository = workAssignmentRepository;
        this.workRepository = workRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<WorkAssignment> getAssignmentsByWorkId(UUID workId) {
        return workAssignmentRepository.findByWorkId(workId);
    }

    @Override
    public List<WorkAssignment> getAssignmentsByUserId(UUID userId) {
        return workAssignmentRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void removeAssignment(UUID workId, UUID userId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        WorkAssignment assignment = workAssignmentRepository.findByWorkAndUser(work, user)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));

        workAssignmentRepository.delete(assignment);
    }
}
