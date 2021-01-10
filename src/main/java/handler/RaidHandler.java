package handler;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import objects.Player;
import objects.PlayerList;
import objects.Raid;
import objects.RaidList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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
    private final String directoryPath;
    final String RAID_PATTERN = "([0-9]{4}\\nRank,Player,ID,Attacks,On-Strat Damage\\n([0-9]{1,2},[0-9|:space_\\p{L}-+]*?,[:alnum]*,[0-9]{1,2},[0-9]*\\n?)*)";
    final Pattern p = Pattern.compile(RAID_PATTERN);
    private static final Logger logger = LogManager.getLogger(RaidHandler.class);


    /**
     * instantiates a handler that parses all channel messages and files into raids if they match the RAID_PATTERN
     *
     * @param guild the guild this handler is responsible for
     */
    public RaidHandler(Guild guild, String directoryPath){

        this.directoryPath = directoryPath;
        File file;
        File[] files;


        logger.info("creating {} for '{}'",
                RaidHandler.class,
                guild.getName());
        try {
            file = new File(directoryPath);
            if(!file.isDirectory()) {
                logger.warn("initial deploy on '{}', trying to create {}",
                        guild.getName(),
                        directoryPath);
                try {
                    if (file.mkdir()) {
                        logger.info("new directory created at {}", directoryPath);
                        for(TextChannel c: guild.getTextChannels()) {
                            for (Message m : c.getIterableHistory()) {
                                Matcher matcher = p.matcher(m.getContentRaw());

                                if (matcher.find()) {
                                    try {
                                        files = new File(directoryPath).listFiles();
                                        for (File f : files) {
                                            if (m.getContentRaw().startsWith(f.getName())) {
                                                logger.debug("found a raid for more than one time (msg: {})", m.getId());
                                                m.addReaction("U+274C").queue();
                                                return;
                                            }
                                        }
                                    } catch (Exception ignored) {
                                        logger.warn("failed to access {}", directoryPath);
                                    }
                                    logger.info("message '{}' ({}) is a raid, proceeding with creation",
                                            m.getContentRaw().substring(0,4),
                                            m.getId());
                                    m.addReaction("U+2705").queue();
                                    this.createRaid(m.getContentRaw());
                                    logger.debug("created new raid from message");
                                }
                            }
                        }
                        if(raids.isEmpty()) {
                            logger.info("there are no recorded raids for '{}' yet", guild.getName());
                        }
                    } else {
                        logger.error("failed to create {}", directoryPath);
                    }
                } catch (Exception exception) {
                    logger.error("trouble when creating a raid: {}", exception.toString());
                }
            } else {
                logger.info("{} exists. evaluating {} files",
                        directoryPath,
                        file.listFiles().length);

                raids = this.parseRaids();
                if(!raids.getList().isEmpty()) {
                    recentRaid = raids.getMostRecentRaid();
                    players = this.determineAllPlayers();
                    activePlayers = this.determineRecentPlayers();
                }
                logger.info("read {} raid(s), totalling {} different players in {}",
                        raids.size(),
                        players.size(),
                        directoryPath);
            }
        } catch (Exception ignored){ //The file does not exist yet
            logger.fatal("{} is missing and could not be created", directoryPath);
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
     * creates a raid object for every raid log file
     * @return list of raids
     */
    private RaidList parseRaids(){
        File[] files = new File(directoryPath).listFiles();
        RaidList list = new RaidList();

        for(File file: files){
            try(BufferedReader br = Files.newBufferedReader(Paths.get(String.valueOf(file)))) {
                CSVReader csvReader = new CSVReader(br);
                Raid raid = new Raid(Integer.parseInt(file.getName().substring(0,1)),Integer.parseInt(file.getName().substring(1,3)),
                        Integer.parseInt(file.getName().substring(3,4)));

                String[] record;
                csvReader.readNext();
                csvReader.readNext();
                while ((record = csvReader.readNext()) != null){
                    raid.addPlayer(createPlayer(record, raid));
                }
                list.addRaid(raid);
            } catch (IOException | CsvValidationException e) {
                e.printStackTrace();
            }
        }
        logger.debug("found {} raid(s)", list.size());
        return list.sort();
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
     * @param csv the message received in Discord
     */
    public void createRaid(String csv){
        String filename = csv.substring(0,4);
        try{
            FileOutputStream fileStream = new FileOutputStream(directoryPath+filename+".txt");
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            writer.write(csv.substring(4));
            writer.close();
            raids.addRaid(new Raid(Integer.parseInt(filename.substring(0,1)),Integer.parseInt(filename.substring(1,3)),
                    Integer.parseInt(filename.substring(3,4))));
            raids = raids.sort();
            recentRaid = raids.getMostRecentRaid();
            players = this.determineAllPlayers();
            activePlayers = this.determineRecentPlayers();
        } catch (IOException exception) {
            logger.error("could not created raid {} in {}", filename, directoryPath);
        }
    }

    /**@deprecated with the addition of RaidLists, they have their own implementation of this method
     * compares all Raids of the LinkedList raids
     * @return the clans highest, latest raid
     */
    @Deprecated
    private Raid getRecentRaid(){
        Raid recent = null;
        try {
            recent = raids.get(0);
        } catch (Exception ignored){
        }
        for(int i = 0; i < raids.size()-1; i++){
            recent = raids.get(i).moreRecent(raids.get(i+1));
        }
        System.out.println("Most recent raid: " + recent);
        return recent;
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
