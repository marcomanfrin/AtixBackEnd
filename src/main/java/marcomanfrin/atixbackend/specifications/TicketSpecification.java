package marcomanfrin.atixbackend.specifications;

import marcomanfrin.atixbackend.entities.Ticket;
import marcomanfrin.atixbackend.enums.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class TicketSpecification {

    public static Specification<Ticket> hasSenderEmail(String senderEmail) {
        return (root, query, criteriaBuilder) -> {
            if (senderEmail == null || senderEmail.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("senderEmail")),
                    "%" + senderEmail.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Ticket> hasOrderNumber(UUID orderNumberId) {
        return (root, query, criteriaBuilder) -> {
            if (orderNumberId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("orderNumber").get("id"), orderNumberId);
        };
    }

    public static Specification<Ticket> hasNameContaining(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Ticket> hasDescriptionContaining(String description) {
        return (root, query, criteriaBuilder) -> {
            if (description == null || description.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + description.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Ticket> hasStatus(TicketStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Ticket> hasCreatedAtAfter(LocalDateTime createdAtFrom) {
        return (root, query, criteriaBuilder) -> {
            if (createdAtFrom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom);
        };
    }

    public static Specification<Ticket> hasCreatedAtBefore(LocalDateTime createdAtTo) {
        return (root, query, criteriaBuilder) -> {
            if (createdAtTo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdAtTo);
        };
    }
}
