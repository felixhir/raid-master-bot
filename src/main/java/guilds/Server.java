package guilds;

import messages.MessageHandling;
import net.dv8tion.jda.api.MessageBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raids.RaidHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.SQLException;
import java.text.DecimalFormat;

@SuppressWarnings("FieldMayBeFinal")
public class Server {
    private String name;
    private TextChannel channel;
    private Guild guild;
    private String prefix;
    private RaidHandler raidHandler;

    private static final Logger logger = LogManager.getLogger(Server.class);

    public Server(TextChannel channel, Guild guild) throws SQLException {
        this.name = guild.getName();
        this.channel = channel;
        this.raidHandler = instantiateRaidHandler();
        this.guild = guild;
        this.prefix = "!";
    }

    public Server(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void sendMessage(Message message){
        this.channel.sendMessage(message).queue();
    }

    public String toString(){
        return "Server " + this.name + " listens to channel: " + this.channel.getName();
    }

    public RaidHandler getRaidHandler() {
        return this.raidHandler;
    }

    private RaidHandler instantiateRaidHandler() throws SQLException {
        return new RaidHandler(channel.getGuild(), this );
    }

    public void receiveMessage(Message message) {
        if(MessageHandling.isRaid(message.getContentRaw())) {
            if(!raidHandler.containsRaid(getRaidName(message.getContentRaw()))) {
                raidHandler.createRaid(message);
            }
            System.out.println("Raid");
        } else if(MessageHandling.isCommand(message.getContentRaw(), prefix)) {
            Message send = new MessageBuilder(MessageHandling.getAnswer(message.getContentRaw(), raidHandler)).build();
            this.sendMessage(send);
        } else {
            logger.info("message '{}' ({}) was ignored as a text message",
                    message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                    message.getId());
        }
    }

    public String getRaidName(String raid) {
        DecimalFormat df = new DecimalFormat("00");
        String firstLine = raid.split("\n")[0].replace("\\", "");

        return firstLine.charAt(0) +
                df.format(Integer.valueOf(firstLine.split("_")[1])) +
                df.format(Integer.valueOf(firstLine.split("_")[2])) +
                this.name.substring(0, 5);
    }
}
