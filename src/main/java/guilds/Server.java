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
    private TextChannel raidChannel;
    private TextChannel defaultChannel;
    private String name;

    private RaidList raids;
    private PlayerList players;
    private char prefix;
    private int afkTimer;

    private static final Logger logger = LogManager.getLogger(Server.class);

    public Server(Guild guild, boolean createdOnJoin) throws SQLException {
        this.guild = guild;
        this.raidChannel = determineTextChannel();
        this.prefix = '!';
        this.afkTimer = 2;
        this.raids = new RaidList();
        this.players = new PlayerList();
        this.name = guild.getName();

        for(TextChannel channel: guild.getTextChannels()) {
            if(channel.canTalk()) {
                this.defaultChannel = channel;
                break;
            }
        }

        if(this.defaultChannel == null) logger.warn("There is no available default channel for SERVER '{}'", guild.getName());


        if(createdOnJoin) {
            this.sendMessage("Hi, this is Raid Master (BETA).\nPlease make sure all of your raids follow the schema specified here: https://github.com/felixhir/raid-master-bot/blob/main/RaidTemplate\n" +
                    "Once you are done, use _!scan_. (you can rerun this command at any time if you missed any raids)");
            DatabaseHandler.add(this);
        } else {
            if(!DatabaseHandler.containsServer(this)) {
                logger.warn("SERVER '{}' added RM during downtime, adding to db", this.name);
                DatabaseHandler.add(this);
            }
            logger.info("pulling data for SERVER '{}'", this.name);
            raids = DatabaseHandler.getRaids(this.guild);
            players = DatabaseHandler.getPlayers(this.guild);
            if(raids.isEmpty()) {
                logger.warn("found 0 RAIDS for SERVER '{}'", this.name);
            } else {
                logger.info("found {} RAIDS for SERVER '{}'",
                        raids.size(),
                        this.name);
            }
        }
    }

    /**
     * NEVER use this for deployment. This is purely for testing purposes.
     */
    public Server() {
        this.prefix = '!';
        this.name = "clanclan";
        this.raids = new RaidList();
        this.players = new PlayerList();
        this.afkTimer = 2;
    }

    public String getName(){
        return this.name;
    }

    public RaidList getRaids() {
        return this.raids;
    }

    public void sendMessage(String message){
        if(this.defaultChannel != null) {
            defaultChannel.sendMessage(message).queue();
        }
    }

    public String toString(){
        return "Server '" + this.name + "' listens to channel: '" + this.raidChannel.getName() + "'";
    }

    public void receiveMessage(Message message) {
        String content = message.getContentRaw();
        if(isRaid(content)) {
            if(!this.raids.containsRaid(getRaidName(content))) {
                Raid raid = this.createRaid(message);
                this.raids.add(raid);
                players = DatabaseHandler.getPlayers(this.guild);
                PlayerList afks = getInactivePlayers();
                sendMessage("You used " + (100-raid.getPotential()) + "% of your attacks.\n" +
                        "Based on the average damage of " + raid.getDpa() + " per attack you could have beaten" +
                        " the raid " + ((raid.getMaxAttacks()-raid.getFewestAttacksNeeded())%4 * 12) +
                        "hours earlier, if everyone attacked " + raid.getFewestAttacksNeeded() + "times");
                if(afks.isEmpty()) {
                    sendMessage("There are no players who didn't attack at all!");
                } else {
                    sendMessage("These players have not participated in the last " + afkTimer + " raids: " + afks);
                }

                message.addReaction("U+2705").queue();
                logger.info("message '{}' ({}) was handled as a new raid",
                        message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                        message.getId());
            } else {
                logger.info("message '{}' ({}) was ignored as an existing raid",
                        message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                        message.getId());
            }
        } else if(isCommand(content)) {
            sendMessage(handleCommand(content));
            logger.info("message '{}' ({}) was handled as command",
                    message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                    message.getId());
        } else {
            logger.info("message '{}' ({}) was ignored as a text message",
                    message.getContentRaw().substring(0,Math.min(5,message.getContentRaw().length())),
                    message.getId());
        }
    }

    public String handleCommand(String messageContent) {
        String command = messageContent.substring(1).split(" ")[0];
        String context;
        try {
            context = messageContent.substring(command.length()+2);
        } catch (Exception ignored) {
            context = "";
        }
        switch (command) {
            case "scan":
                int amount = raids.size();
                logger.info("starting scan for SERVER '{}'", this.name);
                sendMessage("Starting my scan, this may take a while");
                scanMessages();
                raids = DatabaseHandler.getRaids(this.guild);
                players = DatabaseHandler.getPlayers(this.guild);
                logger.info("scanned SERVER '{}' and found {} new RAIDS",
                        this.name,
                        raids.size() - amount);
                return "Finished scanning, I found " + (raids.size() - amount) + " new raid(s).";
            case "setprefix":
                this.setPrefix(context.toCharArray()[0]);
                logger.info("set prefix of SERVER '{}' to '{}'",
                        this.name,
                        this.prefix);
                DatabaseHandler.updateServer(this);
                return "changed prefix to: " + this.prefix;
            case "setafk":
                try {
                    int newTime = Integer.parseInt(context);
                    this.setAfkTimer(newTime);
                    logger.info("set afktimer of SERVER '{}' to '{}'",
                            this.name,
                            this.afkTimer);
                    DatabaseHandler.updateServer(this);
                    return "changed afktimer to: " + this.afkTimer;
                } catch (Exception e) {
                    return "this is not a valid time, nothing changed";
                }
            case "commands":
                return "_" + prefix + "commands:_ returns list of commands\n" +
                       "_" + prefix + "stats <name>:_ returns a players stats\n" +
                       "_" + prefix + "scan:_ scans all messages for raids\n" +
                       "_" + prefix + "setprefix <character>:_ changes the command prefix\n" +
                       "_" + prefix + "setafk <number>:_ sets maximum amount of missed raids";
            case "stats":
                if(context.equals("")) {
                    return "Please make sure to include a name";
                } else {
                    if(players.getPlayerByName(context) == null) {
                        return "Failed to match name";
                    } else {
                        return players.getPlayerByName(context).toString();
                    }
                }
            default:
                return "I couldn't find the command _" + prefix + command + "_. Try _" + prefix + "commands_";
        }
    }

    private void setPrefix(char newPrefix) {
        this.prefix = newPrefix;
    }

    private Raid createRaid(Message message) {
        String[] messageContent = message.getContentRaw().split("\n");
        String[] raidDetails = messageContent[0].replace("\\", "").split("-");
        Raid raid = new Raid(Integer.parseInt(raidDetails[0]),
                Integer.parseInt(raidDetails[1]),
                Integer.parseInt(raidDetails[2]),
                this.name,
                new Date(message.getTimeCreated().toInstant().toEpochMilli()));
        DatabaseHandler.add(raid);
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


    /**
     * Returns the name a raid would be given. This does not require a {@link Raid} to exist and instead uses the String it would be created from.
     *
     * @param raid the entire raid as a String
     * @return the name of the raid
     */
    public String getRaidName(String raid) {
        DecimalFormat df = new DecimalFormat("00");
        String firstLine = raid.split("\n")[0];

        return (firstLine.charAt(0) +
                df.format(Integer.valueOf(firstLine.split("-")[1])) +
                df.format(Integer.valueOf(firstLine.split("-")[2])));
    }

    public void setAfkTimer(int time) {
        this.afkTimer = time;
    }

    public PlayerList getInactivePlayers() {
        PlayerList list = new PlayerList();
        for(Player p: raids.get(Math.max(raids.size()-afkTimer-1,0)).getPlayers()) {
            boolean add = false;
            if(raids.get(Math.max(0,raids.size()-afkTimer-1)).getPlayers().containsPlayerById(p.getId()) &&
                p.getAttacks() != 0) {
                for(int i = Math.max(raids.size()-afkTimer, 0); i < raids.size(); i++) {
                    add = !raids.get(i).getPlayers().containsPlayerById(p.getId()) &&
                            p.getAttacks() != 0;
                }
                if(add) list.add(p);
            }
        }
        return list;
    }


    private TextChannel determineTextChannel() {
        TextChannel channel = this.defaultChannel;
        for(TextChannel t: this.guild.getTextChannels()){
            if(t.getName().equals("raid-master")) {
                channel = t;
            }
        }

        return channel;
    }


    private void scanMessages() {
        int count = 0;
        TextChannel channel = (this.raidChannel != null) ? this.raidChannel : defaultChannel;
        for (Message m : channel.getIterableHistory()) {
            int maxMessages = 600;
            if (count++ < maxMessages) {
                if (this.isRaid(m.getContentRaw())) {
                    if(!raids.containsRaid(getRaidName(m.getContentRaw()))) {
                        raids.add(createRaid(m));
                        m.addReaction("U+2705").queue();
                    } else {
                        logger.info("found a raid for more than one time (msg: {})", m.getId());
                    }
                }
            } else {
                logger.debug("reached 600 messages, ending...");
                return;
            }
        }
    }


    public boolean isRaid(String messageContent) {
        String RAID_PATTERN = "[1-3]-[0-9]{1,2}-[0-9]{1,3}\\n" +
                "Rank,Player,ID,Attacks,On-Strat Damage\\n" +
                "([0-9]{1,2},[^,]*,[a-zA-Z0-9]*,[0-9]*,[0-9]*\\n?)*";
        Pattern p = Pattern.compile(RAID_PATTERN);
        Matcher m = p.matcher(messageContent);

        return m.matches();
    }

    public boolean isCommand(String messageContent) {
        return messageContent.toCharArray()[0] == prefix;
    }

    public void addPlayer(Player p) {
        this.players.add(p);
    }

    public char getPrefix() {
        return this.prefix;
    }

    public int getAfktimer() {
        return this.afkTimer;
    }
}
