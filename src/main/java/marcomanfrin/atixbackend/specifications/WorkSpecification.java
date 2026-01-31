package marcomanfrin.atixbackend.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import marcomanfrin.atixbackend.entities.*;
import marcomanfrin.atixbackend.entities.users.SellerUser;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.WorkStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class WorkSpecification {

    public static Specification<Work> hasAtixClient(UUID clientId) {
        return (root, query, criteriaBuilder) -> {
            if (clientId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("atixClient").get("id"), clientId);
        };
    }

    public static Specification<Work> hasFinalClient(UUID clientId) {
        return (root, query, criteriaBuilder) -> {
            if (clientId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("finalClient").get("id"), clientId);
        };
    }

    public static Specification<Work> hasAnyClient(UUID clientId) {
        return (root, query, criteriaBuilder) -> {
            if (clientId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("atixClient").get("id"), clientId),
                    criteriaBuilder.equal(root.get("finalClient").get("id"), clientId)
            );
        };
    }

    public static Specification<Work> hasSeller(UUID sellerId) {
        return (root, query, criteriaBuilder) -> {
            if (sellerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("seller").get("id"), sellerId);
        };
    }

    public static Specification<Work> hasPlant(UUID plantId) {
        return (root, query, criteriaBuilder) -> {
            if (plantId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("plant").get("id"), plantId);
        };
    }

    public static Specification<Work> hasTicket(UUID ticketId) {
        return (root, query, criteriaBuilder) -> {
            if (ticketId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("ticket").get("id"), ticketId);
        };
    }

    public static Specification<Work> hasStatus(WorkStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Work> hasStatusIn(List<WorkStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Work> hasOrderDateAfter(LocalDate orderDateFrom) {
        return (root, query, criteriaBuilder) -> {
            if (orderDateFrom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), orderDateFrom);
        };
    }

    public static Specification<Work> hasOrderDateBefore(LocalDate orderDateTo) {
        return (root, query, criteriaBuilder) -> {
            if (orderDateTo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), orderDateTo);
        };
    }

    public static Specification<Work> hasExpectedStartDateAfter(LocalDate expectedStartDateFrom) {
        return (root, query, criteriaBuilder) -> {
            if (expectedStartDateFrom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("expectedStartDate"), expectedStartDateFrom);
        };
    }

    public static Specification<Work> hasExpectedStartDateBefore(LocalDate expectedStartDateTo) {
        return (root, query, criteriaBuilder) -> {
            if (expectedStartDateTo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("expectedStartDate"), expectedStartDateTo);
        };
    }

    public static Specification<Work> hasAssignedTechnician(UUID technicianId) {
        return (root, query, criteriaBuilder) -> {
            if (technicianId == null) {
                return criteriaBuilder.conjunction();
            }

            // Join with WorkAssignment to filter by assigned technician
            Join<Work, WorkAssignment> assignmentJoin = root.join("assignments", JoinType.INNER);
            Join<WorkAssignment, User> userJoin = assignmentJoin.join("user", JoinType.INNER);

            // Add distinct to avoid duplicates if a work has multiple assignments
            query.distinct(true);

            return criteriaBuilder.equal(userJoin.get("id"), technicianId);
        };
    }

    public static Specification<Work> hasNameContaining(String name) {
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

    public static Specification<Work> hasBidNumber(String bidNumber) {
        return (root, query, criteriaBuilder) -> {
            if (bidNumber == null || bidNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("bidNumber"), bidNumber);
        };
    }

    public static Specification<Work> hasOrderNumber(String orderNumber) {
        return (root, query, criteriaBuilder) -> {
            if (orderNumber == null || orderNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("orderNumber"), orderNumber);
        };
    }
}
