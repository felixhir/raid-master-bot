import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.math.NumberUtils;

import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {

    RaidHandler handler;
    private static final boolean shouldCleanUp = false;
    private static final String channelName = "raid-input";
    private static TextChannel channel;

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = System.getenv("RAID_MASTER_TOKEN");

        JDA jda = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(),new Main()).build();

        jda.awaitReady();

        for(TextChannel c: jda.getTextChannels()){
            if (c.getName().equals(channelName)){
                channel = c;
            }
        }
        System.out.println("scanning older messages...");
        initialize();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        Message message = event.getMessage();
        String messageStart = "";
        try {
            messageStart = message.getContentRaw().substring(0, 4);
        } catch (Exception ignored){

        }
        if((message.getChannel().getName().equals(channelName)) && (NumberUtils.isCreatable(messageStart))){
            handler = new RaidHandler(message.getContentRaw(), true);
            System.out.println("Received new Raid.");
            message.addReaction("U+2705").queue();
        } else {
            message.delete().queue();
        }
    }

    public static void initialize(){
        String messageStart;

        for(Message m: channel.getIterableHistory()){
            messageStart = "A";
            try {
                messageStart = m.getContentRaw().substring(0, 4);
            } catch (Exception ignored){ }
            if(NumberUtils.isCreatable(messageStart) && m.getReactions().isEmpty()){
                new RaidHandler(m.getContentRaw(), false);
                m.addReaction("U+2705").queue();
            } else if(!NumberUtils.isCreatable(messageStart) && shouldCleanUp) {
                m.delete().queue();
            }
        }
    }

}
