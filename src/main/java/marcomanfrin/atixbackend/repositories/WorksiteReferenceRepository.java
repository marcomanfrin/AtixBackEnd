package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.WorksiteReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorksiteReferenceRepository extends JpaRepository<WorksiteReference, UUID> {
    List<WorksiteReference> findByNameContainingIgnoreCase(String name);
}
