import handler.GuildHandler;
import handler.MessageHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import objects.Server;

import javax.security.auth.login.LoginException;


/**
 * @author felixhir
 */
public class Main extends ListenerAdapter {


    private static final String CHANNEL_NAME = "raid-master";
    private static final String COMMAND_SIGN = "!";
    private static final String BOT_NAME = "TESTING_TOKEN";

    private static GuildHandler guildHandler;

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = System.getenv(BOT_NAME);

        JDABuilder builder = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(),new Main());
        builder.setActivity(Activity.listening(CHANNEL_NAME + " or general"));
        JDA jda = builder.build();

        jda.awaitReady();

        guildHandler = new GuildHandler();
        for(Guild g: jda.getGuilds()){
            TextChannel textChannel = g.getDefaultChannel();
            for(TextChannel t: g.getTextChannels()){
                assert textChannel != null;
                if(t.getName().equals(CHANNEL_NAME)){
                    textChannel = t;
                }
            }
            guildHandler.addServer(new Server(g.getName(), textChannel));
        }
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
            MessageHandler messageHandler = new MessageHandler(message, COMMAND_SIGN, guildHandler.getServerByName(message.getGuild().getName()).getRaidHandler());
            if(messageHandler.isNewRaid()) System.out.println("raid handled\n-----------------");
            if(messageHandler.isCommand()) System.out.println("command " + message.getContentRaw() + " handled\n-----------------");
        }
    }

}
