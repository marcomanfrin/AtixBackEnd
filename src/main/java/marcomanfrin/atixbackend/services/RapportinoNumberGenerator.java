package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.entities.RapportinoCounter;
import marcomanfrin.atixbackend.repositories.RapportinoCounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// Numerazione RFL-{anno}-{seq}: sequenza globale che riparte ogni anno.
// La riga del contatore resta bloccata (SELECT ... FOR UPDATE) fino al commit della creazione,
// quindi due creazioni concorrenti vengono serializzate e un rollback non lascia buchi.
@Service
public class RapportinoNumberGenerator {

    private final RapportinoCounterRepository counterRepository;

    public RapportinoNumberGenerator(RapportinoCounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String nextNumber() {
        int year = LocalDate.now().getYear();
        counterRepository.ensureYear(year);
        RapportinoCounter counter = counterRepository.findForUpdate(year)
                .orElseThrow(() -> new IllegalStateException("Rapportino counter missing for year " + year));
        int value = counter.next();
        return format(year, value);
    }

    static String format(int year, int value) {
        return String.format("RFL-%d-%04d", year, value);
    }
}
