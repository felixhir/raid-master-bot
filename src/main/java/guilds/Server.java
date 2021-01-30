package guilds;

import database.DatabaseHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import players.Player;
import players.PlayerList;
import raids.Raid;
import raids.RaidList;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("FieldMayBeFinal")
public class Server {

    private Guild guild;
    private TextChannel channel;
    private String name;

    private RaidList raids;
    private PlayerList players;
    private String prefix;
    private int afkTimer;

    private static final Logger logger = LogManager.getLogger(Server.class);

    public Server(Guild guild, boolean createdOnJoin) throws SQLException {
        this.guild = guild;
        this.channel = determineTextChannel();
        this.prefix = "!";
        this.afkTimer = 2;
        this.raids = new RaidList();
        this.players = new PlayerList();
        this.name = guild.getName();

        if(createdOnJoin) {
            this.sendMessage("Hi, this is Raid Master.\nPlease make sure all of your raids follow the schema specified here: \n" +
                    "Once you are done, use _!scan_. (Note: This may take some time - you can also rerun this command at any time)");
        } else {
            logger.info("pulling data for SERVER '{}'", this.guild.getName());
            raids = DatabaseHandler.getRaids();
            players = DatabaseHandler.getPlayers(this.guild);
        }
    }

    /**
     * NEVER use this for deployment. This is purely for testing purposes.
     */
    public Server() {
        this.prefix = "!";
        this.name = "clanclan";
    }

    public String getName(){
        return this.guild.getName();
    }

    public void sendMessage(String message){
        this.channel.sendMessage(message).queue();
    }

    public String toString(){
        return "Server '" + this.guild.getName() + "' listens to channel: '" + this.channel.getName() + "'";
    }

    public void receiveMessage(Message message) {
        String content = message.getContentRaw();
        if(isRaid(content)) {
            if(!this.raids.containsRaid(getRaidName(content))) {
                this.createRaid(message);
                PlayerList afks = getInactivePlayers();
                logger.info("message '{}' ({}) was handled as a new raid",
                        message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                        message.getId());
            } else {
                logger.info("message '{}' ({}) was ignored as an existing raid",
                        message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                        message.getId());
            }
        } else if(isCommand(content)) {
            handleCommand(content);
            logger.info("message '{}' ({}) was handled as command",
                    message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                    message.getId());
        } else {
            logger.info("message '{}' ({}) was ignored as a text message",
                    message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                    message.getId());
        }
    }

    private void handleCommand(String messageContent) {
        String command = messageContent.substring(1).split(" ")[0];
        String context;
        try {
            context = messageContent.substring(command.length()+2);
        } catch (Exception ignored) {
            context = "";
        }
        switch (command) {
            case "scan":
                logger.info("starting scan for SERVER '{}'", guild.getName());
                scanMessages();
                break;
            case "setprefix":
                this.setPrefix(context);
                sendMessage("changed prefix to: " + this.prefix);
                logger.info("set prefix of SERVER '{}' to '{}'",
                        guild.getName(),
                        this.prefix);
                break;
            case "commands":
                sendMessage("_" + prefix + "commands:_ returns list of commands\n" +
                        "_" + prefix + "stats <name>:_ returns a players stats\n" +
                        "_" + prefix + "scan:_ scans all messages for raids\n" +
                        "_" + prefix + "setprefix <character>:_ changes the command prefix");
                break;
            case "stats":
                //noinspection IfStatementWithIdenticalBranches
                if(context.equals("")) {
                    sendMessage("Please make sure to include a name");
                    break;
                } else {
                    if(players.getPlayerByName(context) == null) {
                        sendMessage("Failed to match name");
                    } else {
                        sendMessage(players.getPlayerByName(context).toString());
                    }
                    break;
                }
            default:
                sendMessage("I couldn't find the command _" + prefix + command + "_. Try _" + prefix + "commands_");
        }
    }

    private void setPrefix(String newPrefix) {
        this.prefix = newPrefix;
    }

    private Raid createRaid(Message message) {
        String[] messageContent = message.getContentRaw().split("\n");
        String[] raidDetails = messageContent[0].replace("\\", "").split("_");
        Raid raid = new Raid(Integer.parseInt(raidDetails[0]),
                Integer.parseInt(raidDetails[1]),
                Integer.parseInt(raidDetails[2]),
                this.guild.getName(),
                new Date(message.getTimeCreated().toInstant().toEpochMilli()));

        for (int i = 2; i < messageContent.length; i++) {
            Player player = createPlayer(messageContent[i]);
            if (DatabaseHandler.add(raid, player)) {
                raid.addPlayer(player);
                logger.info("successfully added PLAYER '{}' ({}) to db and RAID",
                        player.getRealName(),
                        player.getId());
            } else {
                logger.error("something went wrong adding PLAYER '{}' to RAID '{}'",
                        player.getRealName(),
                        raid.getName());
            }
        }
        return raid;
    }

    public Player createPlayer(String text) {
        String[] s = text.split(",");
        return new Player(Arrays.toString(s[1].getBytes(StandardCharsets.UTF_8)),
                s[2],
                Integer.parseInt(s[3]),
                Integer.parseInt(s[4]));
    }

    public String getRaidName(String raid) {
        DecimalFormat df = new DecimalFormat("00");
        String firstLine = raid.split("\n")[0].replace("\\", "");

        return firstLine.charAt(0) +
                df.format(Integer.valueOf(firstLine.split("_")[1])) +
                df.format(Integer.valueOf(firstLine.split("_")[2])) +
                this.name.substring(0, 5);
    }

    public void setAfkTimer(int time) {
        this.afkTimer = time;
    }

    private PlayerList getInactivePlayers() {
        PlayerList list = new PlayerList();
        for(Player p: raids.getRecentRaid().getPlayers()) {
            boolean add = false;
            if(raids.get(0 + afkTimer).getPlayers().containsPlayerById(p.getId())) {
                for(int i = raids.size()-1; i > 0 + afkTimer; i--) {
                    if(!raids.get(i).getPlayers().containsPlayerById(p.getId())) {
                        add = true;
                    } else {
                        add = false;
                        break;
                    }
                }
                if(add) list.add(p);
            }
        }
        return list;
    }


    private TextChannel determineTextChannel() {
        TextChannel channel = this.guild.getDefaultChannel();
        for(TextChannel t: this.guild.getTextChannels()){
            if(t.getName().equals("raid-master")) {
                channel = t;
            }
        }

        return channel;
    }


    private void scanMessages() {
        for (TextChannel c : this.guild.getTextChannels()) {
            for (Message m : c.getIterableHistory()) {
                if (this.isRaid(m.getContentRaw())) {
                    Raid raid = createRaid(m);
                    if (DatabaseHandler.add(raid)) {
                        m.addReaction("U+2705").queue();
                    } else {
                        logger.debug("found a raid for more than one time (msg: {})", m.getId());
                        m.addReaction("U+274C").queue();
                    }
                }
            }
        }
    }


    public boolean isRaid(String messageContent) {
        String RAID_PATTERN = "[1-3](\\\\)?_[0-9]{1,2}(\\\\)?_[0-9]{1,3}\\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\\n" +
                "([0-9]{1,2},[^,]*,[a-zA-Z0-9]*,[0-9]*,[0-9]*\\n?)*";
        Pattern p = Pattern.compile(RAID_PATTERN);
        Matcher m = p.matcher(messageContent);

        return m.matches();
    }

    public boolean isCommand(String messageContent) {
        logger.debug("checking if message '{}' is a command with prefix: '{}'",
                messageContent.substring(0,Math.min(5,messageContent.length())),
                prefix);
        return messageContent.substring(0,1).equals(prefix);
    }

}
