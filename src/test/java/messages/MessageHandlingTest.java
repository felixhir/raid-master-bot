package messages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raids.RaidHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SuppressWarnings("SpellCheckingInspection")
class MessageHandlingTest {

    private String messageRaidComplete;
    private String messageRaidCompleteBasic;
    private String messageRaidFlawed;
    private String messageText;

    @BeforeEach
    void setUp() {
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
    public void isRaid_recognizesBasicRaid() {
        boolean result = MessageHandling.isRaid(messageRaidCompleteBasic);

        assertTrue(result);
    }

    @Test
    public void isRaid_recognizesFullRaid() {
        boolean result = MessageHandling.isRaid(messageRaidComplete);

        assertTrue(result);
    }

    @Test
    public void isRaid_deniesFlawedRaid() {
        boolean result = MessageHandling.isRaid(messageRaidFlawed);

        assertFalse(result);
    }

    @Test
    public void isCommand_deniesTextMessage() {
        boolean result = MessageHandling.isCommand(messageText,"!");

        assertFalse(result);
    }

    @Test
    public void isCommand_recognizesCommand() {
        boolean result = MessageHandling.isCommand("!stats","!");

        assertTrue(result);
    }

    @Test
    public void isCommand_recognizesCommandWithContext() {
        boolean result = MessageHandling.isCommand("!stats Chad","!");

        assertTrue(result);
    }

    @Test
    public void getAnswer_returnsListOfCommands() {
        String result = MessageHandling.getAnswer("!commands", mock(RaidHandler.class));
        String expected = "_!commands:_ returns list of commands\n" +
                "_!stats:_ returns your stats";

        assertEquals(expected, result);
    }

    @Test
    public void getAnswer_defaultsToUnknwon() {
        String result = MessageHandling.getAnswer("!turkey", mock(RaidHandler.class));
        String expected = "I couldn't find the command _!turkey_. Try _!commands_";

        assertEquals(expected, result);
    }
}