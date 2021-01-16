import handler.DatabaseHandler;
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
import java.sql.SQLException;
import java.util.Objects;


/**
 * @author felixhir
 * @version 1.0 (PRE-RELEASE)
 */
public class Application extends ListenerAdapter {


    private static final String CHANNEL_NAME = "raid-master";
    private static final String COMMAND_SIGN = "!";
    private static final String BOT_TOKEN = "RAID_MASTER_TOKEN";
    private static final String BOT_NAME = "personalTesting";

    private static GuildHandler guildHandler;
    private static JDA jda;
    public static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) throws LoginException, InterruptedException, SQLException {
        logger.debug("starting {}", Application.class);
        String token = System.getenv(BOT_TOKEN);

        new DatabaseHandler();
        guildHandler = new GuildHandler();

        JDABuilder builder = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(), new Application());
        builder.setActivity(Activity.listening(CHANNEL_NAME + " or general"));
        jda = builder.build();

        logger.debug("initialization of variables in {} finished", Application.class);

        jda.awaitReady();

        setupBot();

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

        if(message.getContentRaw().equals("")) return;

        if(!message.getAuthor().getName().equals(BOT_NAME)){
            logger.info("new message '{}' ({}) received from '{}' on '{}'",
                    message.getContentRaw().substring(0, Math.min(message.getContentRaw().length(), 5)),
                    message.getId(),
                    Objects.requireNonNull(message.getMember()).getNickname(),
                    message.getGuild().getName());
            new MessageHandler(message,
                    COMMAND_SIGN,
                    guildHandler.getServerByName(message.getGuild().getName()).getRaidHandler());
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

        try {
            guildHandler.addServer(new Server(channel, guild));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }


    @Override
    public void onGuildLeave(GuildLeaveEvent event){
        logger.info("the server '{}' just removed the bot", event.getGuild().getName());

        Guild guild = event.getGuild();
        int n = guildHandler.getServers().size();
        if(n > guildHandler.getServers().size()) {
            logger.debug("removed '{}' from the {} successfully", guild.getName(), guildHandler.getClass());
        } else {
            logger.warn("could not properly remove '{}' from {}", guild.getName(), guildHandler.getClass());
        }
    }

    public static void setupBot() throws SQLException {
        for(Guild g: jda.getGuilds()){
            TextChannel textChannel = g.getDefaultChannel();
            for(TextChannel t: g.getTextChannels()){
                assert textChannel != null;
                if(t.getName().equals(CHANNEL_NAME)){
                    textChannel = t;
                }
            }
            guildHandler.addServer(new Server(textChannel, g));
        }
        if(guildHandler.getServers().isEmpty()) {
            logger.warn("the bot is not connected to any application despite the API running");
        } else {
            logger.info("the bot is connected to {} server(s)", guildHandler.getServers().size());
        }
    }
}
