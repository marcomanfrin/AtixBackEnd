package marcomanfrin.softwareops.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TECHNICIAN")
public class TechnicianUser extends User {
    // extendable with future new features
}