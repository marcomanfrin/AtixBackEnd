package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.Ticket;
import marcomanfrin.softwareops.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findBySenderEmail(String senderEmail);
    List<Ticket> findByOrderNumberId(UUID orderNumberId);

    long countByStatus(TicketStatus status);
    Page<Ticket> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
