package messages;

import net.dv8tion.jda.api.entities.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raids.RaidHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * provides a set of methods to handle the content of {@link Message} objects
 */
public class MessageHandling {

    public static final Logger logger = LogManager.getLogger(MessageHandling.class);

    private String messageContent;
    private String prefix;

    public static String getAnswer(String messageContent, RaidHandler raidHandler) {
        String prefix = messageContent.substring(0,1);
        String command = messageContent.substring(1).split(" ")[0];
        String context;
        try {
            context = messageContent.substring(command.length()+2);
        } catch (Exception ignored) {
            context = "";
        }


        switch (command) {
            case "commands":
                return "_" + prefix + "commands:_ returns list of commands\n" +
                        "_" + prefix + "stats:_ returns your stats";
            case "stats":
                if(context.equals("")) {
                    return "Please make sure to include a name";
                } else {
                    if(raidHandler.getPlayers().getPlayerByName(context) == null) {
                        return "Failed to match name";
                    } else {
                        return raidHandler.getPlayers().getPlayerByName(context).toString();
                    }
                }
            default:
                return "I couldn't find the command _" + prefix + command + "_. Try _" + prefix + "commands_";
        }
    }

    public static boolean isRaid(String messageContent) {
        String RAID_PATTERN = "[1-3](\\\\)?_[0-9]{1,2}(\\\\)?_[0-9]{1,3}\\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\\n" +
                "([0-9]{1,2},[^,]*,[a-zA-Z0-9]*,[0-9]*,[0-9]*\\n?)*";
        Pattern p = Pattern.compile(RAID_PATTERN);
        Matcher m = p.matcher(messageContent);

        return m.matches();
    }

    public static boolean isCommand(String messageContent, String prefix) {
        logger.debug("checking if message '{}' is a command with prefix: '{}'",
                messageContent.substring(0,Math.min(5,messageContent.length())),
                prefix);
        return messageContent.substring(0,1).equals(prefix);
    }
}
