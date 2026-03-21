package marcomanfrin.atixbackend.config;

import marcomanfrin.atixbackend.DTO.auth.RegisterRequest;
import marcomanfrin.atixbackend.enums.UserRole;
import marcomanfrin.atixbackend.enums.UserType;
import marcomanfrin.atixbackend.repositories.UserRepository;
import marcomanfrin.atixbackend.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final UserService userService;

    public DataInitializer(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
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
    }
}
