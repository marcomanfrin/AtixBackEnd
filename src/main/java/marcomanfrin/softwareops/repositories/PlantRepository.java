package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlantRepository extends JpaRepository<Plant, UUID> {
    List<Plant> findByNameContainingIgnoreCase(String name);
}
