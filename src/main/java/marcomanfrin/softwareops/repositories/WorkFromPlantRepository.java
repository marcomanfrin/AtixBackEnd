package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.WorkFromPlant;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkFromPlantRepository extends JpaRepository<@NonNull WorkFromPlant, @NonNull UUID> {
}
