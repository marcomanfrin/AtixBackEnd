package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.entities.users.User;
import marcomanfrin.atixbackend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class UserColorService {

    // Colori scelti per essere distinguibili tra loro e leggibili con testo bianco
    public static final List<String> PALETTE = List.of(
            "#E53935", "#1E88E5", "#43A047", "#8E24AA",
            "#F4511E", "#00897B", "#3949AB", "#C0CA33",
            "#D81B60", "#6D4C41", "#00ACC1", "#FB8C00",
            "#5E35B1", "#7CB342", "#546E7A", "#FDD835"
    );

    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private final UserRepository userRepository;

    public UserColorService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static boolean isValidColor(String color) {
        return color != null && HEX_COLOR.matcher(color).matches();
    }

    public static String normalize(String color) {
        return color.toUpperCase(Locale.ROOT);
    }

    /**
     * Restituisce il colore della palette usato dal minor numero di utenti attivi
     * (a parità, il primo nell'ordine della palette).
     */
    public String pickColor() {
        return pickColor(userRepository.findByDeletedAtIsNull());
    }

    public static String pickColor(List<User> activeUsers) {
        Map<String, Integer> usage = new HashMap<>();
        for (User user : activeUsers) {
            if (user.getCalendarColor() != null) {
                usage.merge(normalize(user.getCalendarColor()), 1, Integer::sum);
            }
        }
        String best = PALETTE.get(0);
        int bestCount = Integer.MAX_VALUE;
        for (String color : PALETTE) {
            int count = usage.getOrDefault(color, 0);
            if (count < bestCount) {
                best = color;
                bestCount = count;
            }
        }
        return best;
    }
}
