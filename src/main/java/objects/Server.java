package objects;

import handler.RaidHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.SQLException;
import java.util.Locale;

@SuppressWarnings("FieldMayBeFinal")
public class Server {
    private final String name;
    private RaidHandler raidHandler;
    private final TextChannel channel;

    public Server(String n, TextChannel c) throws SQLException {
        this.name = n.toLowerCase(Locale.ROOT).replace(" ", "_");
        this.channel = c;
        this.raidHandler = instantiateRaidHandler();
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
        return new RaidHandler(channel.getGuild(), this.name );
    }
}
