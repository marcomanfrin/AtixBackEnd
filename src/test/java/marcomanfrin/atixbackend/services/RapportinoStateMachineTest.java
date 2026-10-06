package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.enums.RapportinoStatus;
import marcomanfrin.atixbackend.exceptions.InvalidWorkflowTransitionException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static marcomanfrin.atixbackend.enums.RapportinoStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class RapportinoStateMachineTest {

    private final RapportinoStateMachine stateMachine = new RapportinoStateMachine();

    private static final Set<String> ALLOWED = Set.of(
            "DRAFT->AWAITING_SIGNATURE",
            "DRAFT->SIGNED",
            "AWAITING_SIGNATURE->SIGNED",
            "AWAITING_SIGNATURE->DRAFT",
            "SIGNED->VOID"
    );

    @Test
    void onlyTheSpecifiedTransitionsAreAllowed() {
        for (RapportinoStatus from : RapportinoStatus.values()) {
            for (RapportinoStatus to : RapportinoStatus.values()) {
                boolean expected = ALLOWED.contains(from + "->" + to);
                assertEquals(expected, stateMachine.canTransition(from, to), from + " -> " + to);
            }
        }
    }

    @Test
    void voidCannotGoBackToDraft() {
        assertThrows(InvalidWorkflowTransitionException.class, () -> stateMachine.validateTransition(VOID, DRAFT));
    }

    @Test
    void signedCannotGoBackToDraft() {
        assertThrows(InvalidWorkflowTransitionException.class, () -> stateMachine.validateTransition(SIGNED, DRAFT));
    }

    @Test
    void numberFormat() {
        assertEquals("RFL-2026-0001", RapportinoNumberGenerator.format(2026, 1));
        assertEquals("RFL-2026-12345", RapportinoNumberGenerator.format(2026, 12345));
    }
}
