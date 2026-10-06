package marcomanfrin.atixbackend.exceptions;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException() {
        super("Too many requests, please try again later");
    }
}
