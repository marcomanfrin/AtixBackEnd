package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.WorkAssignment;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkAssignmentRepository extends JpaRepository<@NonNull WorkAssignment, @NonNull UUID> {
    List<WorkAssignment> findByWork_Id(UUID workId);
    List<WorkAssignment> findByUser_Id(UUID userId);
    boolean existsByWork_IdAndUser_Id(UUID workId, UUID userId);
    Optional<WorkAssignment> findByWork_IdAndUser_Id(UUID workId, UUID userId);
}
