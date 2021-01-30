package guilds;

import org.junit.jupiter.api.BeforeEach;
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
    private String messageText;

    @BeforeEach
    void setUp() {
        server = new Server();
        raid = new Raid(1,2,3,"clanclan", new Date(10-10-2020));
        messageRaidComplete = "2\\_7\\_1\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Fhaarkas,xb3gkb5,28,18365519\n" +
                "2,Shadoof,ke43b73,24,14169301\n" +
                "3,Ellenor Wolfpack,d5xn57p,24,13664245\n" +
                "4,Call Me Curts,87dgw7n,20,12813849\n" +
                "5,yoinblack,exmb5k5,24,12074820\n" +
                "6,Chewblp,q4req5g,24,12026131\n" +
                "7,Ralfiito,gmqm8ee,24,11419947\n" +
                "8,Lord of Darkness,4mdd2md,28,10321973";
        messageRaidCompleteBasic = "2_01_3\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Fhaarkas,xb3gkb5,28,18365519";
        messageRaidFlawed = "5\\_7\\_1\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Fhaarkas,xb3gkb5,28,18365519\n" +
                "2,Shadoof,ke43b73,24,14169301\n";
        messageText = "Hey guys, check out my stats :)";
    }


    @Test
    public void getRaidName_formsRaidName() {
        String result = server.getRaidName("1\\_2\\_3");

        assertEquals("10203clanc", result);
    }

    @Test
    public void createPlayer_returnsPlayer() {
        String expected = new Player(Arrays.toString("Testname".getBytes(StandardCharsets.UTF_8)),"abcdefg",5,0).toString();
        String result = server.createPlayer("1,Testname,abcdefg,5,0").toString();

        assertEquals(expected, result);
    }

    @Test
    public void isRaid_recognizesBasicRaid() {
        boolean result = server.isRaid(messageRaidCompleteBasic);

        assertTrue(result);
    }

    @Test
    public void isRaid_recognizesFullRaid() {
        boolean result = server.isRaid(messageRaidComplete);

        assertTrue(result);
    }

    @Test
    public void isRaid_deniesFlawedRaid() {
        boolean result = server.isRaid(messageRaidFlawed);

        assertFalse(result);
    }

    @Test
    public void isCommand_deniesTextMessage() {
        boolean result = server.isCommand(messageText);

        assertFalse(result);
    }

    @Test
    public void isCommand_recognizesCommand() {
        boolean result = server.isCommand("!stats");

        assertTrue(result);
    }

    @Test
    public void isCommand_recognizesCommandWithContext() {
        boolean result = server.isCommand("!stats Chad");

        assertTrue(result);
    }
}