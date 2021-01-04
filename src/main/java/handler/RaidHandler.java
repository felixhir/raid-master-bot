package handler;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import objects.Player;
import objects.PlayerList;
import objects.Raid;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The RaidHandler is responsible for the parsing of all saved and read raids as well as the respective players
 */
public class RaidHandler {
    private PlayerList players;
    private PlayerList activePlayers;
    private LinkedList<Raid> raids;
    private Raid recentRaid;
    final String RAID_PATTERN = "([0-9]{4}\\nRank,Player,ID,Attacks,On-Strat Damage\\n([0-9]{1,2},[0-9|:space_\\p{L}-+]*?,[:alnum]*,[0-9]{1,2},[0-9]*\\n?)*)";
    final Pattern p = Pattern.compile(RAID_PATTERN);


    /**
     * instantiates a handler that parses all channel messages and files into raids if they match the RAID_PATTERN
     *
     * @param c the channel this handler will take for initialisation
     */
    public RaidHandler(TextChannel c){

        System.out.println("initialising handler from messages...");
        for(Message m: c.getIterableHistory()){
            if(m.getReactions().isEmpty()){
                Matcher matcher = p.matcher(m.getContentRaw());
                if(matcher.find()){
                    m.addReaction("U+2705").queue();
                    this.createRaid(m.getContentRaw());
                } else {
                    m.addReaction("U+274C").queue();
                }
            }
        }

        System.out.println("initialising handler from files...");
        raids = this.parseRaids();
        recentRaid = this.getRecentRaid();
        players = this.determineAllPlayers();
        activePlayers = this.determineRecentPlayers();
        System.out.println("read " + raids.size() + " raids, totalling " + players.size() + " players");
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
    private LinkedList<Raid> parseRaids(){
        File[] files = new File("./raids").listFiles();
        LinkedList<Raid> list = new LinkedList<>();

        for(File file: files){
            try(BufferedReader br = Files.newBufferedReader(Paths.get(String.valueOf(file)))) {
                CSVReader csvReader = new CSVReader(br);
                Raid raid = new Raid(Integer.parseInt(file.getName().substring(0,1)),Integer.parseInt(file.getName().substring(1,3)),
                        Integer.parseInt(file.getName().substring(3,4)));

                String[] record;
                csvReader.readNext();
                csvReader.readNext();
                while ((record = csvReader.readNext()) != null){
                    raid.addPlayer(createPlayer(record));
                }
                list.add(raid);
            } catch (IOException | CsvValidationException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    /**
     * Creates a Player object from an array of string values
     * @param record consists of a single line from any Raids csv and includes position, name, id, damage, attacks in that order
     * @return a player object
     */
    private Player createPlayer(String[] record){
        return new Player(record[1],record[2],Integer.parseInt(record[3]),Integer.parseInt(record[4]));
    }


    /**
     * saves a String as content of a Raids csv
     * @param csv the message received in Discord
     */
    public void createRaid(String csv){
        String filename = csv.substring(0,4);
        try{
            FileOutputStream fileStream = new FileOutputStream("./raids/"+filename+".txt");
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            writer.write(csv.substring(4));
            writer.close();
            raids = parseRaids();
            recentRaid = this.getRecentRaid();
            players = this.determineAllPlayers();
            activePlayers = this.determineRecentPlayers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * compares all Raids of the LinkedList raids
     * @return the clans highest, latest raid
     */
    private Raid getRecentRaid(){
        Raid recent = null;
        for(int i = 0; i < raids.size()-1; i++){
            recent = raids.get(i).moreRecent(raids.get(i+1));
        }
        return recent;
    }


    /**
     * gives a list of all players who ever took part in any raid
     *
     * @return list of all players
     */
    public PlayerList determineAllPlayers(){
        PlayerList list = new PlayerList();
        for(Raid r: raids){
            for(Player p: r.getPlayers().getList()){
                if(list.containsId(p.getId())){
                    Player updatePlayer = list.getPlayerById(p.getId());
                    list.remove(updatePlayer);
                    list.add(updatePlayer(updatePlayer, p));
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
    private Player updatePlayer(Player old, Player update){
        old.addAttacks(update.getAttacks());
        old.addDamage(update.getDamage());
        old.setName(update.getName());
        return old;
    }

}