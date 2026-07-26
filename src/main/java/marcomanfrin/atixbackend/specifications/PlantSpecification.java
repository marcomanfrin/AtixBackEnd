package marcomanfrin.atixbackend.specifications;

import marcomanfrin.atixbackend.entities.Plant;
import org.springframework.data.jpa.domain.Specification;

public class PlantSpecification {

    public static Specification<Plant> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), pattern)
            );
        };
    }
}
