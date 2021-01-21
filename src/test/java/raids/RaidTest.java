package raids;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import players.Player;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SuppressWarnings("PointlessArithmeticExpression")
class RaidTest {

    private Raid raid;

    @BeforeEach
    public void setUp() {
        raid = new Raid(2,4,1,"Test Clan", new Date(10-10-2020));
    }

    @Test
    public void getName_returnRaidName() {
        String result = raid.getName();
        assertEquals("20401Test ", result);
    }

    @Test
    public void getClanName_returnFormattedName() {
        String result = raid.getClanName();
        assertEquals("Test Clan", result);
    }

    @Test
    public void getDate_returnDate() {
        Date result = raid.getDate();
        assertEquals(new Date(10-10-2020), result);
    }

    @Test
    public void addPlayer_increasesListSize() {
        raid.addPlayer(mock(Player.class));

        int result = raid.getPlayers().size();

        assertEquals(1, result);
    }

    @Test
    public void addPlayer_containsPlayer() {
        Player player = mock(Player.class);
        raid.addPlayer(player);

        Player result = raid.getPlayers().get(0);

        assertEquals(player, result);
    }

    @Test
    public void getTier_returnsTier() {
        int result = raid.getTier();

        assertEquals(2, result);
    }

    @Test
    public void getStage_returnsStage() {
        int result = raid.getStage();

        assertEquals(4, result);
    }

    @Test
    public void getTries_returnsAttempt() {
        int result = raid.getTries();

        assertEquals(1, result);
    }

    @Test
    public void toString_returnsRaidAsString() {
        String result = raid.toString();

        String expectedString = "Raid (Tier 2 - Stage 4) attempt #1 beaten with 0 players";
        assertEquals(expectedString, result);
    }
}