import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * The RaidHandler is responsible for the parsing of all saved and read raids as well as the respective players
 */
public class RaidHandler {
    private PlayerList players;
    private PlayerList activePlayers;
    private LinkedList<Raid> raids;
    private Raid recentRaid;


    /**
     * Instantiates a Handler that either saves a new raid or saves a raid and reads in all existing raids
     *
     * @param raidCsv contains the raid-to-be-saved as a text message sent into a Discord channel
     * @param fromMain tells the Handler if it should only save an old raid or parse everything
     */
    public RaidHandler(String raidCsv, boolean fromMain){
        if (fromMain) {
            players = new PlayerList();
            activePlayers = new PlayerList();
            raids = new LinkedList<>();

            createRaid(raidCsv);
            parseRaids();

            recentRaid = getRecentRaid();

            totalPlayers();
        } else {
            createRaid(raidCsv);
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
     * Creates a Raid instance of every file in the raids-directory
     */
    private void parseRaids(){
        File[] files = new File("./raids").listFiles();

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
                raids.add(raid);
            } catch (IOException | CsvValidationException e) {
                e.printStackTrace();
            }
        }
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
    private void createRaid(String csv){
        String filename = csv.substring(0,4);
        try{
            FileOutputStream fileStream = new FileOutputStream("./raids/"+filename+".txt");
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            writer.write(csv.substring(4));
            writer.close();
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
     * fills the PlayerList players (activePlayers) with all (active) players from all (most recent) raids
     */
    public void totalPlayers(){
        for(Raid r: raids){
            for(Player p: r.getPlayers().getList()){
                if(players.containsId(p.getId())){
                    updatePlayer(p);
                } else {
                    players.addPlayer(p);
                }
            }
        }
        for(Player p: players.getList()){
            try {
                if (!p.getName().equals(recentRaid.getPlayers().getPlayerById(p.getId()).getName())) {
                    p.setName(recentRaid.getPlayers().getPlayerById(p.getId()).getName());
                }
            } catch (Exception ignored){
            }
            if(recentRaid.getPlayers().containsId(p.getId())){
                activePlayers.addPlayer(p);
            }
        }
    }

    /**
     * updates the damage, name and attacks of a Player object from the PlayerList players
     * @param p the Player whose statistics will be used to update an existing player
     */
    private void updatePlayer(Player p){
        Player listedPlayer = players.getPlayerById(p.getId());
        listedPlayer.addAttacks(p.getAttacks());
        listedPlayer.addDamage(p.getDamage());
        listedPlayer.setName(p.getName());
    }

}
