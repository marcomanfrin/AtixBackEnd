package marcomanfrin.atixbackend.entities.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import marcomanfrin.atixbackend.entities.Work;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("SELLER")
public class SellerUser extends User {

    @OneToMany(mappedBy = "seller")
    private List<Work> soldWorks = new ArrayList<>();

    public List<Work> getSoldWorks() {
        return soldWorks;
    }
    public void setSoldWorks(List<Work> soldWorks) {
        this.soldWorks = soldWorks;
    }
}
