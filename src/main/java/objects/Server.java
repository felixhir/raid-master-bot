package objects;

import handler.RaidHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Locale;

@SuppressWarnings("FieldMayBeFinal")
public class Server {
    private final String name;
    private RaidHandler raidHandler;
    private final TextChannel channel;
    public final String directoryPath;

    public Server(String n, TextChannel c){
        this.name = n;
        this.channel = c;
        String directoryName = n.toLowerCase(Locale.ROOT).replace(" ", "_");
        this.directoryPath = "./raids/"+ directoryName + "/";
        this.raidHandler = instantiateRaidHandler();
    }

    public String getName(){
        return this.name;
    }

    public void setRaidHandler(RaidHandler handler){
        this.raidHandler = handler;
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

    public String getDirectoryPath(){
        return directoryPath;
    }

    private RaidHandler instantiateRaidHandler(){
        return new RaidHandler(channel.getGuild(), directoryPath);
    }
}
