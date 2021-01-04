import handler.MessageHandler;
import handler.RaidHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;


/**
 * @author felixhir
 */
public class Main extends ListenerAdapter {


    private static final String CHANNEL_NAME = "raid-input";
    private static final String COMMAND_SIGN = "!";
    private static final String BOT_NAME = "RAID_MASTER_TOKEN";

    private static RaidHandler raidHandler;
    private static TextChannel channel;

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = System.getenv(BOT_NAME);

        JDABuilder builder = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(),new Main());
        builder.setActivity(Activity.listening(CHANNEL_NAME));
        JDA jda = builder.build();

        jda.awaitReady();

        for(TextChannel c: jda.getTextChannels()){
            if (c.getName().equals(CHANNEL_NAME)){
                channel = c;
            }
        }

        raidHandler = new RaidHandler(channel);
    }


    /**
     * contains the bots behaviour upon receiving a message in a previously defined channel
     *
     * if the message was not sent by the bot it will be passed to a message handler, otherwise it is ignored
     *
     * @param event contains the event of a received message
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        Message message = event.getMessage();

        if(!message.getAuthor().getName().equals("Raid Master")){
            MessageHandler messageHandler = new MessageHandler(message, COMMAND_SIGN, raidHandler);
            if(messageHandler.isNewRaid()) System.out.println("raid handled\n-----------------");
            if(messageHandler.isCommand()) System.out.println("command " + message.getContentRaw() + " handled\n-----------------");
        }
    }

}
