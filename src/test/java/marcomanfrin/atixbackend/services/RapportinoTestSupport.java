package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.entities.*;
import marcomanfrin.atixbackend.entities.users.AdministrativeUser;
import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.enums.ClientType;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.WorksiteReferenceRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;

// Fixture condivise dai test d'integrazione dei rapportini (Postgres reale via Testcontainers)
final class RapportinoTestSupport {

    // Un solo container per tutta la JVM di test
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    private RapportinoTestSupport() {}

    static TechnicianUser technician(EntityManager em, String firstName) {
        TechnicianUser user = new TechnicianUser();
        fill(user, firstName, UserRole.USER);
        em.persist(user);
        return user;
    }

    static AdministrativeUser admin(EntityManager em, String firstName) {
        AdministrativeUser user = new AdministrativeUser();
        fill(user, firstName, UserRole.ADMIN);
        em.persist(user);
        return user;
    }

    private static void fill(User user, String firstName, UserRole role) {
        user.setFirstName(firstName);
        user.setLastName("Test");
        user.setEmail(firstName.toLowerCase() + "-" + UUID.randomUUID() + "@test.local");
        user.setPasswordHash("x");
        user.setRole(role);
    }

    static Work work(EntityManager em) {
        Client atix = new Client("Atix Srl", ClientType.ATIX);
        Client finalClient = new Client("Provincia di Bolzano", ClientType.FINAL);
        em.persist(atix);
        em.persist(finalClient);

        Plant plant = new Plant("Palazzo di Giustizia", null, "/nas/pdg", "a", "b", "c");
        em.persist(plant);

        WorksiteReference reference = new WorksiteReference("Mario Rossi", "123", null);
        em.persist(reference);

        Work work = new Work();
        work.setName("Manutenzione PdG");
        work.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        work.setOrderDate(LocalDate.now());
        work.setPlant(plant);
        work.setAtixClient(atix);
        work.setFinalClient(finalClient);
        work.getWorksiteReferenceAssignments().add(new WorksiteReferenceAssignment(work, reference, WorksiteReferenceRole.MAINTENANCE));
        em.persist(work);
        return work;
    }

    static void loginAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}
