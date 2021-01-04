package objects;

import handler.RaidHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Locale;

@SuppressWarnings("FieldMayBeFinal")
public class Server {
    private final String name;
    private RaidHandler raidHandler;
    private final MessageChannel channel;
    public final String directory;

    public Server(String n, MessageChannel c){
        this.name = n;
        this.channel = c;
        this.directory = "./raids/"+n.toLowerCase(Locale.ROOT).replace(" ", "_") + "/";
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
}
