package handler;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import objects.Player;
import objects.Raid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler {

    private final Message message;
    private final MessageChannel channel;
    private final Member author;
    private static String sign;
    private final RaidHandler raidHandler;
    public static final Logger logger = LogManager.getLogger(MessageHandler.class);

    public MessageHandler(Message m, String s, RaidHandler h) {
        message = m;
        channel = m.getChannel();
        author = m.getMember();
        sign = s;
        raidHandler = h;
        logger.info("message '{}' ({}) handler created by '{}'",
                m.getContentRaw().substring(0, Math.min(m.getContentRaw().length(), 5)),
                m.getId(),
                m.getGuild().getName());
        try {
            if(this.isNewRaid()) {
                logger.info("handled raid {} from '{}'",
                        message.getContentRaw().substring(0, Math.min(message.getContentRaw().length(), 5)),
                        message.getGuild().getName());
                return;
            }
        } catch (SQLException ignored) {
        }
        if(this.isCommand()) {
            logger.info("command '{}' ({}) handled", message.getContentRaw(), message.getId());
        } else {
            logger.info("message '{}' ({}) ignored as a text message",
                    message.getContentRaw().substring(0, Math.min(5, message.getContentRaw().length())),
                    message.getId());
        }
    }


    public boolean isCommand(){
        if(message.getContentRaw().substring(0,1).equals(sign)){
            if(raidHandler.getRaidAmount() == 0){
                channel.sendMessage("Sorry, it seems like there are no raids in my database yet!" +
                        " Commands will be available once you give me one or more raids.").queue();
                return false;
            } else {
                this.handleCommand();
                return true;
            }
        } else {
            return false;
        }
    }


    public boolean isNewRaid() throws SQLException {
        String RAID_PATTERN = "([1-3]\\?_[0-9]{1,2}\\?_[0-9]{1,3}\\nRank,Player,ID,Attacks,On-Strat Damage\\n([0-9]{1,2},[0-9|:space_\\p{L}-+]*?,[:alnum]*,[0-9]{1,2},[0-9]*\\n?)*)";
        Pattern p = Pattern.compile(RAID_PATTERN);
        Matcher m = p.matcher(message.getContentRaw());

        if(m.find()){
            String raidDetails = message.getContentRaw().split("\n")[0].replace("\\", "");
            if(DatabaseHandler.containsRaid(new Raid(Integer.parseInt(raidDetails.split("_")[0]),
                    Integer.parseInt(raidDetails.split("_")[1]),
                    Integer.parseInt(raidDetails.split("_")[2]),
                    message.getGuild().getName(),
                    new Date(message.getTimeCreated().toInstant().toEpochMilli())))) {
                message.addReaction("U+274C").queue();
                logger.info("'{}' already exists as a raid, message ({}) will be marked",
                        message.getContentRaw().substring(0,5),
                        message.getId());
                return false;
            }

            logger.info("identified '{}' (id: {}) as new raid",
                    message.getContentRaw().substring(0, Math.min(message.getContentRaw().length(), 5)),
                    message.getId());
            raidHandler.createRaid(message);
            message.addReaction("U+2705").queue();
            return true;
        } else {
            return false;
        }
    }

    public void handleCommand(){
        logger.debug("handling the message '{}' (id: {}) as command",
                message.getContentRaw().substring(1),
                message.getId());
        switch (message.getContentRaw().substring(1)){
            case "stats":
                if(raidHandler.getPlayers().containsName(author.getNickname())) {
                    channel.sendMessage(getPublicPlayerStats(author.getNickname())).queue();
                } else {
                    channel.sendMessage("I couldn't match your nickname to any participants name").queue();
                }
                break;
            case "help":
            case "commands":
                channel.sendMessage(getAvailableCommands()).queue();
                break;
            default:
                channel.sendMessage("no such command was found, check your spelling or use !commands to see available options").queue();
                break;
        }
    }

    private String getAvailableCommands() {
        return "!stats: gives you somewhat detailed information about yourself (your discord nickname must match your ing)" +
                "\n\n!commands: gives you an overview over available commands";
    }

    private String getPublicPlayerStats(String name){
        Player p = raidHandler.getPlayers().getPlayerByName(name);
        DecimalFormat df = new DecimalFormat("#,###");

        return author.getAsMention() +
                "\nDamage: " +
                df.format(p.getDamage()) +
                "\nAttacks: " +
                df.format(p.getAttacks()) +
                "\nDpA: " +
                df.format(p.getDpa()) +
                "\nLatest raid: " ;
    }

    private String getPublicRaidString(Raid r){
        return "T" +
                r.getTier() +
                "S" +
                r.getStage() +
                " attempt #" +
                r.getTries();
    }

}
