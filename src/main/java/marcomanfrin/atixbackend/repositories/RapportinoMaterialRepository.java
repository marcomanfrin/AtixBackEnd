package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.RapportinoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RapportinoMaterialRepository extends JpaRepository<RapportinoMaterial, UUID> {
}
