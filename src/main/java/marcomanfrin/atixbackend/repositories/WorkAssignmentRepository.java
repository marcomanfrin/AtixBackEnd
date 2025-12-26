package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.Work;
import marcomanfrin.atixbackend.entities.WorkAssignment;
import marcomanfrin.atixbackend.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkAssignmentRepository extends JpaRepository<WorkAssignment, UUID> {
    List<WorkAssignment> findByWork(Work work);
    List<WorkAssignment> findByUser(User user);
    Optional<WorkAssignment> findByWorkAndUser(Work work, User user);

    @Query("SELECT wa FROM WorkAssignment wa WHERE wa.work.id = :workId")
    List<WorkAssignment> findByWorkId(@Param("workId") UUID workId);

    @Query("SELECT wa FROM WorkAssignment wa WHERE wa.user.id = :userId")
    List<WorkAssignment> findByUserId(@Param("userId") UUID userId);

    boolean existsByWorkAndUser(Work work, User user);
}
