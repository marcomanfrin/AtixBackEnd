package marcomanfrin.softwareops.exceptions;

import jakarta.validation.ConstraintViolationException;
import marcomanfrin.softwareops.DTO.errors.ErrorDTO;
import marcomanfrin.softwareops.DTO.errors.ErrorWithListDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ErrorWithListDTO handleBadRequest(ValidationException ex) {
        return new ErrorWithListDTO(ex.getMessage(), LocalDateTime.now(), ex.getErrorsList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorWithListDTO handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return new ErrorWithListDTO("Validation error", LocalDateTime.now(), errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorWithListDTO handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return new ErrorWithListDTO("Validation error", LocalDateTime.now(), errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleNotReadable(HttpMessageNotReadableException ex) {
        return new ErrorDTO("Body JSON non valido o non parsabile", LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ErrorDTO("Parametro non valido: " + ex.getName(), LocalDateTime.now());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorDTO handleNotPossible(IllegalStateException ex) {
        return new ErrorDTO(ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
    public ErrorDTO handleUnauthorized(UnauthorizedException ex) {
        return new ErrorDTO(ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 403
    public ErrorDTO handleForbidden(AuthorizationDeniedException ex) {
        return new ErrorDTO("Non hai i permessi per accedere alla risorsa", LocalDateTime.now());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorDTO handleAccessDenied(AccessDeniedException ex) {
        return new ErrorDTO("Non hai i permessi per accedere alla risorsa", LocalDateTime.now());
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409 spesso è più giusto di 400
    public ErrorDTO handleDataIntegrity(DataIntegrityViolationException ex) {
        return new ErrorDTO("Vincolo dati violato (es. valore già esistente)", LocalDateTime.now());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ErrorDTO handleNotFound(NotFoundException ex) {
        return new ErrorDTO(ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public ErrorDTO handleGenericError(Exception ex) {
        ex.printStackTrace();
        return new ErrorDTO("C'è stato un errore lato server, lo risolveremo presto", LocalDateTime.now());
    }
}
