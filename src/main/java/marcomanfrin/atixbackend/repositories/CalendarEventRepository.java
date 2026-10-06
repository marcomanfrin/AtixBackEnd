package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.CalendarEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID>, JpaSpecificationExecutor<CalendarEvent> {

    // Carica partecipanti, creatore e commessa in un'unica query per evitare N+1
    @Override
    @EntityGraph(attributePaths = {"participants", "createdBy", "work", "work.atixClient"})
    List<CalendarEvent> findAll(Specification<CalendarEvent> spec, Sort sort);

    @Override
    @EntityGraph(attributePaths = {"participants", "createdBy", "work", "work.atixClient"})
    Optional<CalendarEvent> findById(UUID id);

    @Modifying
    @Query("UPDATE CalendarEvent e SET e.work = null WHERE e.work.id = :workId")
    int detachFromWork(@Param("workId") UUID workId);
}
