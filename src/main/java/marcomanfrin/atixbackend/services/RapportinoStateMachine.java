package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.enums.RapportinoStatus;
import marcomanfrin.atixbackend.exceptions.InvalidWorkflowTransitionException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

// Nessun override per ruolo: dopo la firma il documento e' immutabile per tutti (correzione = VOID + nuovo)
@Service
public class RapportinoStateMachine {

    private static final Map<RapportinoStatus, Set<RapportinoStatus>> TRANSITIONS = Map.of(
            RapportinoStatus.DRAFT, Set.of(RapportinoStatus.AWAITING_SIGNATURE, RapportinoStatus.SIGNED),
            RapportinoStatus.AWAITING_SIGNATURE, Set.of(RapportinoStatus.SIGNED, RapportinoStatus.DRAFT),
            RapportinoStatus.SIGNED, Set.of(RapportinoStatus.VOID),
            RapportinoStatus.VOID, Set.of()
    );

    public boolean canTransition(RapportinoStatus currentStatus, RapportinoStatus targetStatus) {
        return TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus);
    }

    public void validateTransition(RapportinoStatus currentStatus, RapportinoStatus targetStatus) {
        if (!canTransition(currentStatus, targetStatus)) {
            throw new InvalidWorkflowTransitionException(currentStatus.name(), targetStatus.name());
        }
    }
}
