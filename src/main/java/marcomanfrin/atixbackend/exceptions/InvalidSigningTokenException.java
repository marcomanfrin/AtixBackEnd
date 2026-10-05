package marcomanfrin.atixbackend.exceptions;

// Token di firma sconosciuto, scaduto, gia' usato o revocato: un'unica eccezione e un'unica risposta,
// cosi' l'endpoint pubblico non rivela quale condizione si e' verificata.
public class InvalidSigningTokenException extends RuntimeException {
    public static final String MESSAGE = "This signing link is not valid or has expired";

    public InvalidSigningTokenException() {
        super(MESSAGE);
    }
}
