import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.math.NumberUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author felixhir
 */
public class Main extends ListenerAdapter {

    RaidHandler handler;
    private static final String channelName = "raid-input";
    private static TextChannel channel;
    final String RAID_PATTERN = "([0-9]{4}\\nRank,Player,ID,Attacks,On-Strat Damage\\n([0-9]{1,2},[0-9|:space_\\p{L}-+]*?,[:alnum]*,[0-9]{1,2},[0-9]*\\n?)*)";
    final Pattern p = Pattern.compile(RAID_PATTERN);

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = System.getenv("RAID_MASTER_TOKEN");

        JDA jda = JDABuilder.createDefault(token).addEventListeners(new ReadyListener(),new Main()).build();

        jda.awaitReady();

        for(TextChannel c: jda.getTextChannels()){
            if (c.getName().equals(channelName)){
                channel = c;
            }
        }

        initialize();
    }

    /**
     * contains the bots behaviour upon receiving a message in a previously defined channel
     *
     * if the message is a raid it will be saved as a .txt file and the handler will be called
     *
     * otherwise the message is deleted
     *
     * @param event contains the event of a received message
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        Message message = event.getMessage();

        Matcher m = p.matcher(message.getContentRaw());

        File[] files = null;
        try {
            files = new File("./raids").listFiles();
        } catch (Exception ignored){

        }
        if((message.getChannel().getName().equals(channelName) && m.find())){
            assert files != null;
            for (File f: files){
                if((message.getContentRaw().substring(0,4)+".txt").equals((f.getName()))){
                    System.out.println("raid already exists - deleting message...");
                    message.delete().queue();
                    return;
                }
            }

            System.out.println("received new raid.");
            handler = new RaidHandler(message.getContentRaw(), true);
            message.addReaction("U+2705").queue();
        } else {
            System.out.println("no new raid detected");
            message.addReaction("U+274C").queue();
        }
    }

    /**
     * when the bot connects to a server it will go over all messages of a defined channel to check for older raids
     */
    public static void initialize(){
        System.out.println("scanning older messages...");
        String messageStart;

        for(Message m: channel.getIterableHistory()){
            messageStart = "A";
            try {
                messageStart = m.getContentRaw().substring(0, 4);
            } catch (Exception ignored){ }
            if(NumberUtils.isCreatable(messageStart) && m.getReactions().isEmpty()){
                new RaidHandler(m.getContentRaw(), false);
                m.addReaction("U+2705").queue();
            }
        }

        System.out.println("captured every raid!");
    }

}
