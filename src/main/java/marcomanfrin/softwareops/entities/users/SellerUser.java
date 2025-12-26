package marcomanfrin.softwareops.entities.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SELLER")
public class SellerUser extends User {
    // extendable with future new features
}
