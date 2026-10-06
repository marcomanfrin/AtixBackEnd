package marcomanfrin.atixbackend.specifications;

import marcomanfrin.atixbackend.entities.Rapportino;
import marcomanfrin.atixbackend.enums.RapportinoStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class RapportinoSpecification {

    public static Specification<Rapportino> hasWork(UUID workId) {
        return (root, query, criteriaBuilder) -> {
            if (workId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("work").get("id"), workId);
        };
    }

    public static Specification<Rapportino> hasTechnician(UUID technicianId) {
        return (root, query, criteriaBuilder) -> {
            if (technicianId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("technician").get("id"), technicianId);
        };
    }

    public static Specification<Rapportino> hasStatus(RapportinoStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Rapportino> interventionDateFrom(LocalDate from) {
        return (root, query, criteriaBuilder) -> {
            if (from == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("interventionDate"), from);
        };
    }

    public static Specification<Rapportino> interventionDateTo(LocalDate to) {
        return (root, query, criteriaBuilder) -> {
            if (to == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("interventionDate"), to);
        };
    }

    // Visibilita' per utenti non amministrativi: tecnico assegnato oppure autore
    public static Specification<Rapportino> visibleTo(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("technician").get("id"), userId),
                    criteriaBuilder.equal(root.get("createdBy").get("id"), userId)
            );
        };
    }
}
