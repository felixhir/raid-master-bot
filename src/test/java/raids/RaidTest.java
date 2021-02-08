package raids;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import players.Player;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SuppressWarnings("PointlessArithmeticExpression")
class RaidTest {

    private Raid raid;
    private Player player;
    private Player player2;

    @BeforeEach
    public void setUp() {
        raid = new Raid(2,4,1,"Test Clan", new Date(10-10-2020));
        player = new Player(Arrays.toString("DummyPlayer".getBytes(StandardCharsets.UTF_8)),
                "abcdefg",
                12,
                24000);
        player2 = new Player(Arrays.toString("player".getBytes(StandardCharsets.UTF_8)),
                "1234567",
                10,
                10000);
    }

    @Test
    public void getName_returnRaidName() {
        String result = raid.getName();
        assertEquals("20401", result);
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

        String expectedString = "Tier 2 - Stage 4 - attempt #1";
        assertEquals(expectedString, result);
    }

    @Test
    void getTotalAttacks_returnsAttacks() {
        raid.addPlayer(player);
        int result = raid.getTotalAttacks();

        assertEquals(12, result);
    }

    @Test
    void getTotalDamage_returnsDamage() {
        raid.addPlayer(player);
        int result = raid.getTotalDamage();

        assertEquals(24000, result);
    }

    @Test
    void getDpa_returnsAverage() {
        raid.addPlayer(player);
        raid.addPlayer(player2);
        double result = raid.getDpa();

        assertEquals(1545, result);
    }

    @Test
    void getMaxAttacks_returnsMax() {
        raid.addPlayer(player2);

        assertEquals(10,raid.getMaxAttacks());
        raid.addPlayer(player);

        assertEquals(12, raid.getMaxAttacks());
    }

    @Test
    void getMissedAttacks_returnsAmount() {
        raid.addPlayer(player);
        raid.addPlayer(player2);
        int result = raid.getMissedAttacks();

        assertEquals(2, result);
    }

    @Test
    void getPotential_returnsPotential() {
        raid.addPlayer(player2);
        raid.addPlayer(player);
        double result = raid.getPotential();
        double expected = 2.0/24*100;

        assertEquals(expected, result);
    }

    @Test
    void fewestAttacks_returnsAttacks() {
        raid.addPlayer(player);
        raid.addPlayer(player2);
        int result = raid.getFewestAttacksNeeded();

        assertEquals(11, result);
    }

    @Test
    void factify_doesProperMath() {
        raid.addPlayer(player2);
        raid.addPlayer(new Player(Arrays.toString("yes".getBytes(StandardCharsets.UTF_8)),
                "aah",
                20,
                500000));
        String result = raid.factify();
        String expected = "You used 75% of your attacks," +
                " if everyone attacked 15 times you could have saved 12 hours.";

        assertEquals(expected, result);
    }
}