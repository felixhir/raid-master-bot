import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.math.NumberUtils;

import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {

    RaidHandler handler;

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = System.getenv("RAID_MASTER_TOKEN");

        JDA jda = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(),new Main()).build();

        jda.awaitReady();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        Message message = event.getMessage();
        String messageStart = "";
        try {
            messageStart = message.getContentRaw().substring(0, 4);
        } catch (Exception e){

        }
        if((message.getChannel().getName().equals("raid-input")) && (NumberUtils.isCreatable(messageStart))){
            handler = new RaidHandler(message.getContentRaw());
            System.out.println("Received new Raid.");
            message.addReaction("U+2705").queue();
        } else {
            message.delete().queue();
            return;
        }
    }

}
