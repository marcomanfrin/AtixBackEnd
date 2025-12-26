package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.Ticket;
import marcomanfrin.softwareops.enums.TicketStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<@NonNull Ticket, @NonNull UUID> {
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByClient_Id(UUID clientId);
    List<Ticket> findByPlant_Id(UUID plantId);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    Page<Ticket> findByClient_Id(UUID clientId, Pageable pageable);
    Page<Ticket> findByPlant_Id(UUID plantId, Pageable pageable);

    long countByStatus(TicketStatus status);
    List<Ticket> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Query with JOIN FETCH to prevent N+1
    @Query("""
        SELECT DISTINCT t FROM Ticket t
        LEFT JOIN FETCH t.client
        LEFT JOIN FETCH t.plant
        WHERE t.id = :id
    """)
    Optional<Ticket> findByIdWithRelations(@Param("id") UUID id);

    @Query("""
        SELECT DISTINCT t FROM Ticket t
        LEFT JOIN FETCH t.client
        LEFT JOIN FETCH t.plant
    """)
    List<Ticket> findAllWithRelations();
}
