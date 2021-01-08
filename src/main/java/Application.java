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
import java.util.Objects;


/**
 * @author felixhir
 * @version 0.1
 */
public class Application extends ListenerAdapter {


    private static final String CHANNEL_NAME = "raid-master";
    private static final String COMMAND_SIGN = "!";
    private static final String BOT_TOKEN = "TESTING_TOKEN";
    private static final String BOT_NAME = "personalTesting";

    private static GuildHandler guildHandler;
    private static JDA jda;
    public static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) throws LoginException, InterruptedException {
        logger.debug("starting {}", Application.class);
        String token = System.getenv(BOT_TOKEN);
        guildHandler = new GuildHandler();

        JDABuilder builder = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(), new Application());
        builder.setActivity(Activity.listening(CHANNEL_NAME + " or general"));
        jda = builder.build();

        logger.debug("initialization of variables in {} finished", Application.class);

        jda.awaitReady();

        try {
            File rootDirectory = new File("./raids");
            if(!rootDirectory.isDirectory()){
                if(rootDirectory.mkdir()) {
                    logger.info("{}} created, running setup", rootDirectory);
                    setupBot();
                } else {
                    logger.fatal("could not create {}", rootDirectory);
                    //System.out.println("Something went HORRIBLY wrong");
                    jda.shutdownNow();
                    System.exit(-1);
                }
            } else {
                logger.info("{} accessed, running setup", rootDirectory);
                setupBot();
            }
        } catch (Exception ignored){
            logger.fatal("could not access root directory or create it, shutting down");
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

        if(!message.getAuthor().getName().equals(BOT_NAME)){
            logger.info("new message '{}' ({}) received from '{}' on '{}'",
                    message.getContentRaw().substring(0, Math.min(message.getContentRaw().length(), 5)),
                    message.getId(),
                    Objects.requireNonNull(message.getMember()).getNickname(),
                    message.getGuild().getName());
            MessageHandler messageHandler = new MessageHandler(message,
                    COMMAND_SIGN,
                    guildHandler.getServerByName(message.getGuild().getName()).getRaidHandler(),
                    guildHandler.getServerByName(message.getGuild().getName()).getDirectoryPath());
            if(messageHandler.isNewRaid()) {
                logger.info("handled raid {} from '{}'",
                        message.getContentRaw().substring(0, Math.min(message.getContentRaw().length(), 5)),
                        message.getGuild().getName());
            }
            if(messageHandler.isCommand()) {
                logger.info("command '{}' ({}) handled", message.getContentRaw(), message.getId());
            } else {
                logger.info("message '{}' ({}) ignored as a text message",
                        message.getContentRaw().substring(0, Math.min(5, message.getContentRaw().length())),
                        message.getId());
            }
        }
    }


    @Override
    public void onGuildJoin(GuildJoinEvent event){
        logger.info("the bot was added to '{}'", event.getGuild().getName());

        Guild guild = event.getGuild();
        TextChannel channel = guild.getDefaultChannel();

        for(TextChannel t: guild.getTextChannels()){
            if(CHANNEL_NAME.equals(t.getName())) channel = t;
        }
        assert channel != null;
        logger.debug("the channel '{}' will be used as default for {}", channel.getName(), guild.getName());

        guildHandler.addServer(new Server(guild.getName(), channel));
    }


    @Override
    public void onGuildLeave(GuildLeaveEvent event){
        logger.info("the server '{}' just removed the bot", event.getGuild().getName());

        Guild guild = event.getGuild();
        int n = guildHandler.getServers().size();
        guildHandler.removeServer(guild.getName());
        if(n > guildHandler.getServers().size()) {
            logger.debug("removed '{}' from the {} successfully", guild.getName(), guildHandler.getClass());
        } else {
            logger.warn("could not properly remove '{}' from {}", guild.getName(), guildHandler.getClass());
        }
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
        if(guildHandler.getServers().isEmpty()) {
            logger.warn("the bot is not connected to any application despite the API running");
        } else {
            logger.info("the bot is connected to {} servers", guildHandler.getServers().size());
        }
    }
}
