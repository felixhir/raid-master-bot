package players;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(Arrays.toString("Chad Thundercock".getBytes(StandardCharsets.UTF_8)),
                "abcdefg",
                100,
                666000);
    }

    @Test
    void addDamage_increases() {
        player.addDamage(500);
        int result = player.getDamage();

        assertEquals(666500, result);
    }

    @Test
    void addAttacks_increases() {
        player.addAttacks(5);
        int result = player.getAttacks();

        assertEquals(105, result);
    }

    @Test
    void getRealName_returnsReadableName() {
        String result = player.getRealName();

        assertEquals("Chad Thundercock", result);
    }

    @Test
    void getByteName_returnsGibberish() {
        String result = player.getByteName();
        String expected = Arrays.toString("Chad Thundercock".getBytes(StandardCharsets.UTF_8));

        assertEquals(expected, result);
    }

    @Test
    void setRealName_changesBothNames() {
        player.setRealName("Lad");
        String result1 = player.getRealName();
        String result2 = player.getByteName();
        String expected1 = "Lad";
        String expected2 = Arrays.toString("Lad".getBytes(StandardCharsets.UTF_8));

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
    }
}