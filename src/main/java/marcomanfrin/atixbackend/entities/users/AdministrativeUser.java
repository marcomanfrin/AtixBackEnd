package marcomanfrin.atixbackend.entities.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMINISTRATION")
public class AdministrativeUser extends User {
    // extendable with future new features
}
