package marcomanfrin.atixbackend.specifications;

import marcomanfrin.atixbackend.entities.Client;
import marcomanfrin.atixbackend.enums.ClientType;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecification {

    public static Specification<Client> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
        };
    }

    public static Specification<Client> hasType(ClientType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }
}
