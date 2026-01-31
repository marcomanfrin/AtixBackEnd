package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.WorkStatus;
import marcomanfrin.atixbackend.exceptions.InvalidWorkflowTransitionException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class WorkStateMachine {

    private static final Map<WorkStatus, Set<WorkStatus>> STANDARD_TRANSITIONS = Map.of(
            WorkStatus.SCHEDULED, Set.of(WorkStatus.IN_PROGRESS),
            WorkStatus.IN_PROGRESS, Set.of(WorkStatus.CLOSED, WorkStatus.SCHEDULED),
            WorkStatus.CLOSED, Set.of(WorkStatus.INVOICED, WorkStatus.IN_PROGRESS),
            WorkStatus.INVOICED, Set.of()
    );

    public void validateTransition(WorkStatus currentStatus, WorkStatus targetStatus, UserRole userRole) {
        if (currentStatus == targetStatus) {
            throw new InvalidWorkflowTransitionException("Work is already in status " + currentStatus);
        }

        if (userRole == UserRole.OWNER) {
            if (currentStatus == WorkStatus.INVOICED) {
                throw new InvalidWorkflowTransitionException(
                        "Cannot transition from INVOICED. Work is finalized.");
            }
            return;
        }

        Set<WorkStatus> allowed = STANDARD_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new InvalidWorkflowTransitionException(currentStatus.name(), targetStatus.name());
        }
    }
}
