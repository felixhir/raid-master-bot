package guilds;

import raids.RaidHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.SQLException;
import java.util.Locale;

@SuppressWarnings("FieldMayBeFinal")
public class Server {
    private final String name;
    private final TextChannel channel;
    private final Guild guild;

    private RaidHandler raidHandler;

    public Server(TextChannel channel, Guild guild) throws SQLException {
        this.name = guild.getName().toLowerCase(Locale.ROOT).replace(" ", "_");
        this.channel = channel;
        this.raidHandler = instantiateRaidHandler();
        this.guild = guild;
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
}
