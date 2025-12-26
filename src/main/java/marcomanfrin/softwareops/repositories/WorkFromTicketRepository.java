package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.WorkFromTicket;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkFromTicketRepository extends JpaRepository<@NonNull WorkFromTicket, @NonNull UUID> {
}
