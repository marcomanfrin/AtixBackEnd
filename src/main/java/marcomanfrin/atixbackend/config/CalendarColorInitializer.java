package marcomanfrin.atixbackend.config;

import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.repositories.UserRepository;
import marcomanfrin.atixbackend.services.UserColorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Assegna un colore calendario agli utenti che non ne hanno uno. Idempotente:
 * agli avvii successivi non trova utenti senza colore e non modifica nulla.
 */
@Component
public class CalendarColorInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(CalendarColorInitializer.class);

    private final UserRepository userRepository;

    public CalendarColorInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<User> active = userRepository.findByDeletedAtIsNull();
        List<User> missing = userRepository.findAll().stream()
                .filter(u -> u.getCalendarColor() == null)
                .sorted(Comparator.comparing(User::getLastName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        if (missing.isEmpty()) {
            return;
        }
        for (User user : missing) {
            user.setCalendarColor(UserColorService.pickColor(active));
        }
        userRepository.saveAll(missing);
        logger.info("Assigned calendar colour to {} user(s)", missing.size());
    }
}
