package marcomanfrin.atixbackend.exceptions;

public class InvalidWorkflowTransitionException extends RuntimeException {

    public InvalidWorkflowTransitionException(String currentStatus, String targetStatus) {
        super("Invalid workflow transition from " + currentStatus + " to " + targetStatus);
    }

    public InvalidWorkflowTransitionException(String message) {
        super(message);
    }
}
