package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.ChecklistTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChecklistTemplateItemRepository extends JpaRepository<ChecklistTemplateItem, UUID> {
    List<ChecklistTemplateItem> findByActiveTrueOrderByPositionAsc();

    boolean existsByCode(String code);
}
