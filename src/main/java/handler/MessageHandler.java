package handler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler {

    private static Message message;
    private static MessageChannel channel;
    private static String sign;

    public MessageHandler(Message m, String s) {
        message = m;
        channel = m.getChannel();
        sign = s;
    }


    public boolean isCommand(){
        if(message.getContentRaw().substring(0,1).equals(sign)){
            return true;
        } else {
            return false;
        }
    }


    public boolean isRaid(){
        String RAID_PATTERN = "([0-9]{4}\\nRank,Player,ID,Attacks,On-Strat Damage\\n([0-9]{1,2},[0-9|:space_\\p{L}-+]*?,[:alnum]*,[0-9]{1,2},[0-9]*\\n?)*)";
        Pattern p = Pattern.compile(RAID_PATTERN);
        Matcher m = p.matcher(message.getContentRaw());

        if(m.find()){
            return true;
        } else {
            return false;
        }
    }

    public void handleCommand(){
        switch (message.getContentRaw().substring(1)){
            default:
                channel.sendMessage("no such command was found, check your spelling or use !commands to see available options").queue();
                break;
        }
    }

}
