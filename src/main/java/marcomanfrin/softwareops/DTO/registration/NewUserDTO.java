package marcomanfrin.softwareops.DTO.registration;

import jakarta.validation.constraints.*;
import marcomanfrin.softwareops.enums.UserRole;
import marcomanfrin.softwareops.enums.UserType;

import java.time.LocalDate;

public record NewUserDTO(
        @NotBlank(message = "Il nome è un campo obbligatorio")
        @Size(min = 2, max = 30, message = "Il nome deve avere una lunghezza tra 2 e 30 caratteri")
        String firstName,

        @NotBlank(message = "Il cognome è un campo obbligatorio")
        @Size(min = 2, max = 30, message = "Il cognome deve avere una lunghezza tra 2 e 30 caratteri")
        String lastName,

        @NotBlank(message = "Lo username è un campo obbligatorio")
        @Size(min = 3, max = 30, message = "Lo username deve avere una lunghezza tra 3 e 30 caratteri")
        String userName,

        @NotBlank(message = "L'email è un campo obbligatorio")
        @Email(message = "L'email non è nel formato corretto")
        String email,

        @NotBlank(message = "La password è un campo obbligatorio")
        @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$", message = "La password deve contenere almeno 8 caratteri, una lettera maiuscola, una minuscola e un numero")
        String password,

        @NotNull(message = "Il ruolo è un campo obbligatorio")
        UserRole role,

        @NotNull(message = "Il tipo utente è un campo obbligatorio")
        UserType userType
        ) {}
