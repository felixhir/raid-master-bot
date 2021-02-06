import database.DatabaseHandler;
import guilds.Server;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.LinkedList;


/**
 * @author felixhir
 * @version 1.0 (PRE-RELEASE)
 */
public class Application extends ListenerAdapter {

    private static final String CHANNEL_NAME = "raid-master";
    private static final String BOT_TOKEN = "RAID_MASTER_TOKEN";
    private static final String BOT_NAME = "Raid Master";

    private static StatusUpdater runner;
    private static JDA jda;
    private static LinkedList<Server> servers;

    public static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) throws LoginException, InterruptedException, SQLException {
        logger.debug("starting {}", Application.class);
        String token = System.getenv(BOT_TOKEN);
        new DatabaseHandler();
        servers = new LinkedList<>();

        JDABuilder builder = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(), new Application());
        builder.setActivity(Activity.listening(CHANNEL_NAME + " or general"));
        jda = builder.build();

        logger.debug("initialization of variables in {} finished", Application.class);

        jda.awaitReady();
        runner = new StatusUpdater(jda);
        runner.run();

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
        runner.addMessage();
        Message message = event.getMessage();

        if(message.getContentRaw().equals("")) return;

        if(message.getAuthor().getId().equals("224178281790832641")) {
            for(Server s: servers) {
                s.sendMessage(message.getContentRaw());
            }
        }

        if(!message.getAuthor().getName().equals(BOT_NAME)){
            String senderType = message.isFromGuild() ? "on '" + message.getGuild().getName() + "'"
                    : "as private message";
            logger.info("new message '{}' ({}) received from '{}' {}",
                    message.getContentRaw().substring(0, Math.min(message.getContentRaw().length(), 5)),
                    message.getId(),
                    message.getAuthor().getName(),
                    senderType);
            if(message.isFromGuild()) {
                for(Server server: servers) {
                    if(message.getGuild().getName().equals(server.getName())) {
                        server.receiveMessage(message);
                    }
                }
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

        try {
            servers.add(new Server(guild, true));
        } catch (SQLException exception) {
            runner.addException();
            exception.printStackTrace();
        }
    }


    @Override
    public void onGuildLeave(GuildLeaveEvent event){
        logger.info("the server '{}' just removed the bot", event.getGuild().getName());

        Guild guild = event.getGuild();
        servers.removeIf(server -> server.getName().equals(guild.getName()));
        logger.info("removed '{}' from the serverlist successfully", guild.getName());
    }

    public static void setupBot() throws SQLException {
        for(Guild g: jda.getGuilds()){
            servers.add(new Server(g, false));
        }
        if(servers.isEmpty()) {
            logger.warn("the bot is not connected to any application despite the API running");
        } else {
            logger.info("the bot is connected to {} server(s)", servers.size());
        }
    }
}
