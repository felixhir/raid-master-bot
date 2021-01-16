package handler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The RaidHandler is responsible for the parsing of all saved and read raids as well as the respective players
 */
public class RaidHandler {
    private PlayerList players;
    private PlayerList activePlayers;
    private RaidList raids;
    private Raid recentRaid;
    private final Server server;
    final String RAID_PATTERN = "([0-9]_[0-9]{1,2}_[0-9]{1,3}\\nRank,Player,ID,Attacks,On-Strat Damage\\n([0-9]{1,2},[0-9|:space_\\p{L}-+]*?,[:alnum]*,[0-9]{1,2},[0-9]*\\n?)*)";
    final Pattern p = Pattern.compile(RAID_PATTERN);
    private static final Logger logger = LogManager.getLogger(RaidHandler.class);


    /**
     * instantiates a handler that parses all channel messages and files into raids if they match the RAID_PATTERN
     *
     * @param guild the guild this handler is responsible for
     */
    public RaidHandler(Guild guild, Server server) throws SQLException {
        this.raids = new RaidList();
        this.server = server;
        logger.info("creating {} for '{}'",
                RaidHandler.class.getName(),
                guild.getName());

        if(DatabaseHandler.containsServer(this.server)) {
            logger.info("'{}' already exists in database, skipping initialization", guild.getName());
            //here goes reading from db :)

        } else {
            logger.warn("initial deploy on '{}', running through messages", guild.getName());
            for(TextChannel c: guild.getTextChannels()){
                for(Message m: c.getIterableHistory()){
                    Matcher matcher = p.matcher(m.getContentRaw());
                    if(matcher.find()){
                        String raidDetails = m.getContentRaw().split("\n")[0];
                        if(DatabaseHandler.add(new Raid(Integer.parseInt(raidDetails.split("_")[0]),
                                Integer.parseInt(raidDetails.split("_")[1]),
                                Integer.parseInt(raidDetails.split("_")[2]),
                                server.getName(),
                                new Date(m.getTimeCreated().toInstant().toEpochMilli())))) {
                            logger.info("message '{}' ({}) is a raid, added it to the db",
                                    m.getContentRaw().substring(0,5),
                                    m.getId());
                            m.addReaction("U+2705").queue();
                        } else {
                            logger.debug("found a raid for more than one time (msg: {})", m.getId());
                            m.addReaction("U+274C").queue();
                        }
                    }
                }
            }
            if(raids.isEmpty()) {
                logger.info("there are no recorded raids for '{}' yet", guild.getName());
            }
        }
    }


    /**
     * gives a PlayerList object of all raid participants
     * @return a list of all players
     */
    public PlayerList getPlayers(){
        return this.players;
    }


    /**
     * gives a PlayerList object of all participants of the most recent raid
     * @return a list of the recent players
     */
    public PlayerList getActivePlayers(){
        return this.activePlayers;
    }


    /**
     * Creates a Player object from an array of string values
     * @param record consists of a single line from any Raids csv and includes position, name, id, damage, attacks in that order
     * @return a player object
     */
    private Player createPlayer(String[] record, Raid raid){
        return new Player(record[1], record[2], Integer.parseInt(record[3]), Integer.parseInt(record[4]), raid);
    }


    /**
     * saves a String as content of a Raids csv
     * @param message the message received in Discord
     */
    public void createRaid(Message message) {
        String raidDetails = message.getContentRaw().split("\n")[0];
        Raid raid = new Raid(Integer.parseInt(raidDetails.split("_")[0]),
                Integer.parseInt(raidDetails.split("_")[1]),
                Integer.parseInt(raidDetails.split("_")[2]),
                server.getName(),
                new Date(message.getTimeCreated().toInstant().toEpochMilli()));

        if(DatabaseHandler.add(raid)){
            this.raids.addRaid(raid);
        }

        this.raids = raids.sort();
        this.players = this.determineAllPlayers();
        this.activePlayers = this.determineRecentPlayers();

    }

    /**
     * gives a list of all players who ever took part in any raid
     *
     * @return list of all players
     */
    public PlayerList determineAllPlayers(){
        PlayerList list = new PlayerList();
        for(Raid r: raids.getList()){
            for(Player p: r.getPlayers().getList()){
                if(list.containsId(p.getId())){
                    Player updatePlayer = list.getPlayerById(p.getId());
                    list.remove(updatePlayer);
                    list.add(updatePlayer(updatePlayer, p, r));
                } else {
                    list.addPlayer(p);
                }
            }
        }
        for(Player p: list.getList()){
            try {
                if (!p.getName().equals(recentRaid.getPlayers().getPlayerById(p.getId()).getName())) {
                    p.setName(recentRaid.getPlayers().getPlayerById(p.getId()).getName());
                }
            } catch (Exception ignored){
            }
        }
        return list;
    }


    /**
     * reads out the players from the most recent raid with their totalled stats
     *
     * @return list of players who took part in the most recent raid
     */
    private PlayerList determineRecentPlayers(){
        PlayerList list = new PlayerList();
        for(Player p: players.getList()){
            if(recentRaid.getPlayers().containsId(p.getId())){
                list.addPlayer(p);
            }
        }
        return list;
    }


    /**
     * Takes a player and updates them with the stats of another player
     *
     * @param old the player to update
     * @param update the player which stats to use for the update
     * @return a player with updated statistics
     */
    private Player updatePlayer(Player old, Player update, Raid r){
        old.addAttacks(update.getAttacks());
        old.addDamage(update.getDamage());
        old.setName(update.getName());
        old.addParticipation();
        old.addRaid(r);
        return old;
    }

    public int getRaidAmount(){
        return raids.size();
    }

}
