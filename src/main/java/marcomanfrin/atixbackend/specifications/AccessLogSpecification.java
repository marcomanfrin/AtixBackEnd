package marcomanfrin.atixbackend.specifications;

import marcomanfrin.atixbackend.entities.AccessLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccessLogSpecification {

    public static Specification<AccessLog> hasUserId(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return cb.conjunction();
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<AccessLog> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<AccessLog> hasSuccess(Boolean success) {
        return (root, query, cb) -> {
            if (success == null) return cb.conjunction();
            return cb.equal(root.get("success"), success);
        };
    }

    public static Specification<AccessLog> hasTimestampAfter(LocalDateTime from) {
        return (root, query, cb) -> {
            if (from == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("timestamp"), from);
        };
    }

    public static Specification<AccessLog> hasTimestampBefore(LocalDateTime to) {
        return (root, query, cb) -> {
            if (to == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("timestamp"), to);
        };
    }
}
