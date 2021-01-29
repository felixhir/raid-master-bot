package messages;

import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raids.RaidHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SuppressWarnings("SpellCheckingInspection")
class MessageHandlerTest {

    private String textMessage;
    private String commandMessage;
    private String raidMessage1;
    private String raidMessage2;
    private String incompleteMessage1;
    private String incompleteMessage2;
    private MessageHandler handler;

    @BeforeEach
    public void setup() {
        textMessage = "Hey guys, I just did something great!";
        commandMessage = "!stats";
        raidMessage1 = "2_8_1\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Hound,d5dwkrk,28,20960282";
        raidMessage2 = "2\\_7\\_1\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Fhaarkas,xb3gkb5,28,18365519\n" +
                "2,Shadoof,ke43b73,24,14169301\n" +
                "3,Ellenor Wolfpack,d5xn57p,24,13664245\n" +
                "4,Call Me Curts,87dgw7n,20,12813849\n" +
                "5,yoinblack,exmb5k5,24,12074820\n" +
                "6,Chewblp,q4req5g,24,12026131\n" +
                "7,Ralfiito,gmqm8ee,24,11419947\n" +
                "8,Lord of Darkness,4mdd2md,28,10321973";
        incompleteMessage1 = "2_8_1\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Hound,d5dwkrk,28,20960282\n" +
                "2,Fhaarkas,xb3gkb5,24,19864017\n" +
                "3,Ellenor Wolfpack,d5xn57p,28,18984527\n" +
                "4Shadoof,ke43b73,24,16422400\n" +
                "5,Sword Master,y64mpd8,28,14599081\n" +
                "6,Chewblp,q4req5g,24,13488957";
        incompleteMessage2 = "21_8_1\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\n" +
                "1,Hound,d5dwkrk,28,20960282\n" +
                "2,Fhaarkas,xb3gkb5,24,19864017\n" +
                "3,Ellenor Wolfpack,d5xn57p,28,18984527\n" +
                "4,Shadoof,ke43b73,24,16422400\n" +
                "5,Sword Master,y64mpd8,28,14599081\n" +
                "6,Chewblp,q4req5g,24,13488957";
        //handler = new MessageHandler(mock(Message.class),"!",mock(RaidHandler.class));
    }

    @Test
    public void matchRaid_deniesNonRaids() {
        boolean resultText = MessageHandler.matchRaid(textMessage);
        boolean resultCommand = MessageHandler.matchRaid(commandMessage);

        assertFalse(resultText);
        assertFalse(resultCommand);
    }

    @Test
    public void matchRaid_acceptsCompleteRaids() {
        boolean result1 = MessageHandler.matchRaid(raidMessage1);
        boolean result2 = MessageHandler.matchRaid(raidMessage2);

        assertTrue(result1);
        //assertTrue(result2);
    }

    @Test
    public void matchRaid_deniesMalformedStage() {
        boolean result = MessageHandler.matchRaid(incompleteMessage2);

        assertFalse(result);
    }

    @Test
    public void matchRaid_deniesMalformedPlayer() {
        boolean result = MessageHandler.matchRaid(incompleteMessage1);

        assertFalse(result);
    }

}