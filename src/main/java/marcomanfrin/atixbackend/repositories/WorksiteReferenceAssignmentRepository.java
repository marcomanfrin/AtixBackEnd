package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.Work;
import marcomanfrin.atixbackend.entities.WorksiteReference;
import marcomanfrin.atixbackend.entities.WorksiteReferenceAssignment;
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorksiteReferenceAssignmentRepository extends JpaRepository<WorksiteReferenceAssignment, UUID> {
    List<WorksiteReferenceAssignment> findByWork(Work work);
    List<WorksiteReferenceAssignment> findByWorksiteReference(WorksiteReference worksiteReference);
    List<WorksiteReferenceAssignment> findByRole(WorksiteReferenceRole role);

    Optional<WorksiteReferenceAssignment> findByWorkAndWorksiteReference(Work work, WorksiteReference worksiteReference);

    @Query("SELECT wra FROM WorksiteReferenceAssignment wra WHERE wra.work.id = :workId")
    List<WorksiteReferenceAssignment> findByWorkId(@Param("workId") UUID workId);

    @Query("SELECT wra FROM WorksiteReferenceAssignment wra WHERE wra.worksiteReference.id = :worksiteReferenceId")
    List<WorksiteReferenceAssignment> findByWorksiteReferenceId(@Param("worksiteReferenceId") UUID worksiteReferenceId);

    boolean existsByWorkAndWorksiteReference(Work work, WorksiteReference worksiteReference);
}
