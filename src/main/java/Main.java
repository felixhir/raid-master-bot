import handler.GuildHandler;
import handler.MessageHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import objects.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.File;


/**
 * @author felixhir
 * @version 0.1
 */
public class Main extends ListenerAdapter {


    private static final String CHANNEL_NAME = "raid-master";
    private static final String COMMAND_SIGN = "!";
    private static final String BOT_NAME = "TESTING_TOKEN";

    private static GuildHandler guildHandler;
    private static JDA jda;
    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = System.getenv(BOT_NAME);
        guildHandler = new GuildHandler();

        JDABuilder builder = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(), new Main());
        builder.setActivity(Activity.listening(CHANNEL_NAME + " or general"));
        jda = builder.build();

        jda.awaitReady();

        try {
            File rootDirectory = new File("./raids");
            if(!rootDirectory.isDirectory()){
                if(rootDirectory.mkdir()) {
                    logger.info("root dir created, running setup");
                    setupBot();
                } else {
                    logger.error("could not create {}", rootDirectory);
                    //System.out.println("Something went HORRIBLY wrong");
                    jda.shutdownNow();
                    System.exit(-1);
                }
            } else {
                logger.info("root dir accessed, running setup");
                setupBot();
            }
        } catch (Exception ignored){
            logger.error("trouble accessing root dir");
            System.exit(-1);
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
            MessageHandler messageHandler = new MessageHandler(message, COMMAND_SIGN, guildHandler.getServerByName(message.getGuild().getName()).getRaidHandler(), guildHandler.getServerByName(message.getGuild().getName()).getDirectoryPath());
            if(messageHandler.isNewRaid()) System.out.println("raid handled\n-----------------");
            if(messageHandler.isCommand()) System.out.println("command " + message.getContentRaw() + " handled\n-----------------");
        }
    }


    @Override
    public void onGuildJoin(GuildJoinEvent event){
        Guild guild = event.getGuild();
        System.out.println("Raid Master was added to '" + guild.getName() + "'");
        TextChannel channel = guild.getDefaultChannel();

        for(TextChannel t: guild.getTextChannels()){
            if(CHANNEL_NAME.equals(t.getName())) channel = t;
        }

        guildHandler.addServer(new Server(guild.getName(), channel));
    }


    @Override
    public void onGuildLeave(GuildLeaveEvent event){
        Guild guild = event.getGuild();
        System.out.println("'" + guild.getName() + "' just removed Raid Master from their server");
        guildHandler.removeServer(guild.getName());
    }

    public static void setupBot(){
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
        System.out.println("\n- - - - - DONE | BOT RUNNING - - - - -\n");
    }
}
