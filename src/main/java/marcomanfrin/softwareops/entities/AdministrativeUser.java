package marcomanfrin.softwareops.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class AdministrativeUser extends User {
    // extendable with future new features
}
