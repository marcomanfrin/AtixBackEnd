package marcomanfrin.atixbackend.entities;

import jakarta.persistence.*;

// Contatore annuale per la numerazione RFL-{anno}-{seq}: una riga per anno, letta con lock pessimistico.
@Entity
@Table(name = "rapportino_counters")
public class RapportinoCounter {

    @Id
    private Integer year;

    @Column(nullable = false)
    private int lastValue;

    public RapportinoCounter() {}

    public RapportinoCounter(Integer year) {
        this.year = year;
        this.lastValue = 0;
    }

    public int next() {
        return ++lastValue;
    }

    public Integer getYear() {
        return year;
    }

    public int getLastValue() {
        return lastValue;
    }
}
