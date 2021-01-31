package players;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerListTest {

    private PlayerList list;
    private Player playerOne;
    private Player playerTwo;

    @BeforeEach
    void setUp() {
        list = new PlayerList();
        playerOne = new Player(Arrays.toString("Chad Thundercock".getBytes(StandardCharsets.UTF_8)),
                "abcdefg",
                100,
                666000);
        playerTwo = new Player(Arrays.toString("Virgin".getBytes(StandardCharsets.UTF_8)),
                "000aaa",
                1,
                0);
    }

    @Test
    void add_addsPlayers() {
        boolean result1 = list.add(playerOne);
        boolean result2 = list.add(playerTwo);

        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    void add_doesntAddPlayerTwice() {
        list.add(playerOne);
        list.add(playerOne);
        int result = list.size();

        assertEquals(1, result);
    }

    @Test
    void add_updatesPlayer() {
        list.add(playerOne);
        list.add(new Player(Arrays.toString("Test".getBytes(StandardCharsets.UTF_8)), "abcdefg", 1,1));
        Player result = list.get(0);

        assertEquals("Test", result.getRealName());
        assertEquals("abcdefg", result.getId());
        assertEquals(101, result.getAttacks());
        assertEquals(666001, result.getDamage());
    }

    @Test
    void toString_returnsNames() {
        list.add(playerOne);
        list.add(playerTwo);
        String result = list.toString();

        assertEquals("'Chad Thundercock', 'Virgin'", result);
    }

    @Test
    void getPlayerById_returnsPlayer() {
        list.add(playerOne);
        Player result = list.getPlayerById("abcdefg");

        assertEquals(playerOne, result);
    }

    @Test
    void getPlayerById_returnsNull() {
        list.add(playerOne);
        Player result = list.getPlayerById("way too long of an id");

        assertNull(result);
    }

    @Test
    void getPlayerByName_returnsPlayer() {
        list.add(playerTwo);
        String result = list.getPlayerByName("Virgin").getRealName();

        assertEquals(playerTwo.getRealName(), result);
    }

    @Test
    void getPlayerByName_returnsNull() {
        list.add(playerTwo);
        list.add(playerOne);
        Player result = list.getPlayerByName("This name does not exist");

        assertNull(result);
    }

    @Test
    void getTopPlayers_givesTopTwo() {
        list.add(playerTwo);
        list.add(playerOne);
        String result = list.getTopPlayers().get(0).getRealName();

        assertEquals("Chad Thundercock", result);
    }
}