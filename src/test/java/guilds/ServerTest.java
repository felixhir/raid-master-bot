package guilds;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import players.Player;
import raids.Raid;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class ServerTest {

    private Server server;
    private Raid raid;
    private String messageRaidComplete;
    private String messageRaidCompleteBasic;
    private String messageRaidFlawed;
    private Player playerOne;

    @BeforeEach
    void setUp() {
        server = new Server();
        raid = new Raid(1,2,3,"clanclan", new Date(10-10-2020));
        messageRaidComplete = "2-7-1\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Fhaarkas,xb3gkb5,28,18365519\n" +
                "2,Shadoof,ke43b73,24,14169301\n" +
                "3,Ellenor Wolfpack,d5xn57p,24,13664245\n" +
                "4,Call Me Curts,87dgw7n,20,12813849\n" +
                "5,yoinblack,exmb5k5,24,12074820\n" +
                "6,Chewblp,q4req5g,24,12026131\n" +
                "7,Ralfiito,gmqm8ee,24,11419947\n" +
                "8,Lord of Darkness,4mdd2md,28,10321973";
        messageRaidCompleteBasic = "2-01-3\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Fhaarkas,xb3gkb5,28,18365519";
        messageRaidFlawed = "5-7-1\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Fhaarkas,xb3gkb5,28,18365519\n" +
                "2,Shadoof,ke43b73,24,14169301\n";
        playerOne = new Player(Arrays.toString("Chad Thundercock".getBytes(StandardCharsets.UTF_8)),
                "abcdefg",
                100,
                666000);
    }


    @Test
    void getRaidName_formsRaidName() {
        String result = server.getRaidName(messageRaidComplete);

        assertEquals("20701", result);
    }

    @Test
    void createPlayer_returnsPlayer() {
        String expected = new Player(Arrays.toString("Testname".getBytes(StandardCharsets.UTF_8)),"abcdefg",5,0).toString();
        String result = server.createPlayer("1,Testname,abcdefg,5,0").toString();

        assertEquals(expected, result);
    }

    @Test
    void isRaid_recognizesBasicRaid() {
        boolean result = server.isRaid(messageRaidCompleteBasic);

        assertTrue(result);
    }

    @Test
    void isRaid_recognizesFullRaid() {
        boolean result = server.isRaid(messageRaidComplete);

        assertTrue(result);
    }

    @Test
    void isRaid_deniesFlawedRaid() {
        boolean result = server.isRaid(messageRaidFlawed);

        assertFalse(result);
    }

    @Test
    void isCommand_deniesTextMessage() {
        boolean result = server.isCommand("Hey guys check out this link :)");

        assertFalse(result);
    }

    @Test
    void isCommand_recognizesCommand() {
        boolean result = server.isCommand("!stats");

        assertTrue(result);
    }

    @Test
    void isCommand_recognizesCommandWithContext() {
        boolean result = server.isCommand("!stats Chad");

        assertTrue(result);
    }

    @Test
    void getAfks_returnsAfkPlayer() {
        Player playerTwo = new Player(Arrays.toString("Virgin".getBytes(StandardCharsets.UTF_8)),
                "000aaa",
                1,
                0);
        server.getRaids().add(new Raid(1,2,3,"clanclan", new Date(1-10-2020)));
        server.getRaids().get(0).addPlayer(playerOne);
        server.getRaids().get(0).addPlayer(playerTwo);
        server.getRaids().get(0).addPlayer(new Player(Arrays.toString("No attacks".getBytes(StandardCharsets.UTF_8)),
                "abcabc",
                2,
                100));
        server.getRaids().add(new Raid(2,2,3,"clanclan", new Date(10-10-2020)));
        server.getRaids().get(1).addPlayer(playerOne);
        server.getRaids().get(1).addPlayer(new Player(Arrays.toString("No attacks".getBytes(StandardCharsets.UTF_8)),
                "abcabc",
                0,
                0));
        server.getRaids().add(new Raid(3,2,3,"clanclan", new Date(12-10-2020)));
        server.getRaids().get(2).addPlayer(playerOne);
        String result = server.getInactivePlayers().toString();

        assertEquals("'Virgin', 'No attacks'", result);
    }

    @Test
    void handleCommand_returnsCommands() {
        String result = server.handleCommand("!commands");
        String expected = "_!commands:_ returns list of commands\n" +
                "_!stats <name>:_ returns a players stats\n" +
                "_!scan:_ scans all messages for raids\n" +
                "_!setprefix <character>:_ changes the command prefix\n" +
                "_!setafk <number>:_ sets maximum amount of missed raids";

        assertEquals(expected, result);
    }

    @Test
    void handleCommand_returnsStats() {
        server.addPlayer(playerOne);
        String result = server.handleCommand("!stats Chad Thundercock");
        String expected = "Chad Thundercock (abcdefg) has dealt 666.000 damage with 100 attacks (6.660 DpA).";

        assertEquals(expected, result);
    }

    @Test
    void handleCommand_cantFindPlayer() {
        String result = server.handleCommand("!stats doesntExist");
        String expected = "Failed to match name";

        assertEquals(expected, result);
    }

    @Disabled("unhandled DB interaction")
    @Test
    void handleCommand_setsIntAfk() {
        String result = server.handleCommand("!setafk 5");
        String expected = "changed afktimer to: 5";

        assertEquals(expected, result);
    }

    @Test
    void handleCommand_setsStringAfk() {
        String result = server.handleCommand("!setafk b");
        String expected = "this is not a valid time, nothing changed";

        assertEquals(expected, result);
    }

    @Disabled("unhandled DB interaction")
    @Test
    void handleCommand_changesPrefix() {
        String result = server.handleCommand("!setprefix ~");
        String expected = "changed prefix to: ~";

        assertEquals(expected, result);
    }

    @Test
    void handleCommand_defaultsToUnknown() {
        String result = server.handleCommand("!thiscommanddoesntexist");
        String expected = "I couldn't find the command _!thiscommanddoesntexist_. Try _!commands_";

        assertEquals(expected, result);
    }
}