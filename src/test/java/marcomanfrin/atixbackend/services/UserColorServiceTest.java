package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.entities.users.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserColorServiceTest {

    private static User userWithColor(String color) {
        User user = new TechnicianUser();
        user.setCalendarColor(color);
        return user;
    }

    @Test
    void paletteHasAtLeastTwelveValidDistinctColours() {
        assertTrue(UserColorService.PALETTE.size() >= 12);
        assertEquals(UserColorService.PALETTE.size(), UserColorService.PALETTE.stream().distinct().count());
        UserColorService.PALETTE.forEach(c -> assertTrue(UserColorService.isValidColor(c), c));
    }

    @Test
    void picksFirstPaletteColourWhenNobodyHasOne() {
        assertEquals(UserColorService.PALETTE.get(0), UserColorService.pickColor(List.of()));
    }

    @Test
    void picksFirstUnusedColour() {
        List<User> users = List.of(
                userWithColor(UserColorService.PALETTE.get(0)),
                userWithColor(UserColorService.PALETTE.get(1).toLowerCase()),
                userWithColor(null)
        );
        assertEquals(UserColorService.PALETTE.get(2), UserColorService.pickColor(users));
    }

    @Test
    void picksLeastUsedColourWhenPaletteExhausted() {
        List<User> users = new ArrayList<>();
        for (String color : UserColorService.PALETTE) {
            users.add(userWithColor(color));
            users.add(userWithColor(color));
        }
        // il terzo colore ha un utente in meno degli altri
        users.removeIf(u -> u.getCalendarColor().equals(UserColorService.PALETTE.get(3)) && users.indexOf(u) % 2 == 0);
        assertEquals(UserColorService.PALETTE.get(3), UserColorService.pickColor(users));
    }

    @Test
    void validatesHexFormat() {
        assertTrue(UserColorService.isValidColor("#1E88E5"));
        assertTrue(UserColorService.isValidColor("#1e88e5"));
        assertFalse(UserColorService.isValidColor("blue"));
        assertFalse(UserColorService.isValidColor("#12345"));
        assertFalse(UserColorService.isValidColor("1E88E5"));
        assertFalse(UserColorService.isValidColor(null));
    }
}
