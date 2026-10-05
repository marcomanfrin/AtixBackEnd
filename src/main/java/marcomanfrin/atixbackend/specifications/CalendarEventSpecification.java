package marcomanfrin.atixbackend.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import marcomanfrin.atixbackend.entities.CalendarEvent;
import marcomanfrin.atixbackend.entities.users.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CalendarEventSpecification {

    // Sovrapposizione con la finestra semiaperta [from, to): start < to AND end > from
    public static Specification<CalendarEvent> overlaps(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.lessThan(root.get("startAt"), to),
                criteriaBuilder.greaterThan(root.get("endAt"), from)
        );
    }

    public static Specification<CalendarEvent> hasParticipant(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return participantExists(root, query, criteriaBuilder, List.of(userId));
        };
    }

    public static Specification<CalendarEvent> participantIn(Collection<UUID> userIds) {
        return (root, query, criteriaBuilder) -> {
            if (userIds == null || userIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return participantExists(root, query, criteriaBuilder, userIds);
        };
    }

    // EXISTS invece di JOIN: niente righe duplicate e nessuna interferenza con il fetch dei partecipanti
    private static Predicate participantExists(
            Root<CalendarEvent> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            Collection<UUID> userIds) {
        Subquery<UUID> subquery = query.subquery(UUID.class);
        Root<CalendarEvent> subRoot = subquery.from(CalendarEvent.class);
        Join<CalendarEvent, User> participant = subRoot.join("participants");
        subquery.select(subRoot.get("id"))
                .where(
                        criteriaBuilder.equal(subRoot, root),
                        participant.get("id").in(userIds)
                );
        return criteriaBuilder.exists(subquery);
    }
}
