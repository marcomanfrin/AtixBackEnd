package marcomanfrin.atixbackend.config;

import marcomanfrin.atixbackend.DTO.auth.RegisterRequest;
import marcomanfrin.atixbackend.entities.ChecklistTemplateItem;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.UserType;
import marcomanfrin.atixbackend.repositories.ChecklistTemplateItemRepository;
import marcomanfrin.atixbackend.repositories.UserRepository;
import marcomanfrin.atixbackend.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    // Voci checklist dei rapportini (dal prototipo): codice stabile, etichetta IT, etichetta EN
    private static final List<String[]> CHECKLIST_SEED = List.of(
        new String[]{"PLANT_CHECK", "Verifica funzionamento impianto", "Plant operation check"},
        new String[]{"FIRMWARE_UPDATE", "Aggiornamento firmware / software", "Firmware / software update"},
        new String[]{"PANEL_WIRING_CHECK", "Verifica collegamenti elettrici in quadro", "Electrical panel wiring check"},
        new String[]{"ACTUATOR_TEST", "Test servocomandi", "Actuator test"},
        new String[]{"SENSOR_TEST", "Test e controllo sensori", "Sensor test and check"},
        new String[]{"BUS_TEST", "Test e controllo bus di comunicazione", "Communication bus test and check"}
    );

    private final UserRepository userRepository;
    private final UserService userService;
    private final ChecklistTemplateItemRepository checklistTemplateItemRepository;

    public DataInitializer(UserRepository userRepository, UserService userService,
                           ChecklistTemplateItemRepository checklistTemplateItemRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.checklistTemplateItemRepository = checklistTemplateItemRepository;
    }

    @Override
    public void run(String... args) {
        // Create default OWNER user if database is empty
        if (userRepository.count() == 0) {
            logger.info("No users found in database. Creating default OWNER user...");

            RegisterRequest defaultOwner = new RegisterRequest(
                "Admin",
                "User",
                "admin@atixbackend.com",
                "Admin123!",
                UserRole.OWNER,
                UserType.ADMINISTRATION
            );

            userService.createUserWithoutEmail(defaultOwner);

            logger.info("Default OWNER user created successfully!");
            logger.info("Email: admin@atixbackend.com");
            logger.info("Password: Admin123!");
            logger.info("IMPORTANT: Please change these credentials after first login!");
        } else {
            logger.info("Users already exist in database. Skipping default user creation.");
        }

        seedChecklistTemplate();
    }

    // Idempotente per codice: le voci gia' presenti (anche se modificate o disattivate) non vengono toccate
    private void seedChecklistTemplate() {
        int created = 0;
        for (int i = 0; i < CHECKLIST_SEED.size(); i++) {
            String[] item = CHECKLIST_SEED.get(i);
            if (!checklistTemplateItemRepository.existsByCode(item[0])) {
                checklistTemplateItemRepository.save(new ChecklistTemplateItem(item[0], item[1], item[2], i + 1));
                created++;
            }
        }
        if (created > 0) {
            logger.info("Seeded {} checklist template items.", created);
        }
    }
}
