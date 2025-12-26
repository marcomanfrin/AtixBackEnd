package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.WorksiteReference;
import marcomanfrin.softwareops.entities.WorksiteReferenceAssignment;
import marcomanfrin.softwareops.entities.users.User;
import marcomanfrin.softwareops.enums.WorksiteReferenceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorksiteReferenceAssignmentRepository extends JpaRepository<WorksiteReferenceAssignment, UUID> {
    List<WorksiteReferenceAssignment> findByWorksiteReference(WorksiteReference worksiteReference);
    List<WorksiteReferenceAssignment> findByUser(User user);
    List<WorksiteReferenceAssignment> findByRole(WorksiteReferenceRole role);

    Optional<WorksiteReferenceAssignment> findByWorksiteReferenceAndUser(WorksiteReference worksiteReference, User user);

    @Query("SELECT wra FROM WorksiteReferenceAssignment wra WHERE wra.worksiteReference.id = :worksiteReferenceId")
    List<WorksiteReferenceAssignment> findByWorksiteReferenceId(@Param("worksiteReferenceId") UUID worksiteReferenceId);

    @Query("SELECT wra FROM WorksiteReferenceAssignment wra WHERE wra.user.id = :userId")
    List<WorksiteReferenceAssignment> findByUserId(@Param("userId") UUID userId);

    boolean existsByWorksiteReferenceAndUser(WorksiteReference worksiteReference, User user);
}
