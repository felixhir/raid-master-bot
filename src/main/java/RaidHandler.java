import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class RaidHandler {
    PlayerList players;
    PlayerList activePlayers;
    LinkedList<Raid> raids;
    Raid recentRaid;

    public RaidHandler(String raidCsv, boolean fromMain){
        if(fromMain){
            players = new PlayerList();
            activePlayers = new PlayerList();
            raids = new LinkedList<>();

            createRaid(raidCsv);
            parseRaids();

            recentRaid = getRecentRaid();

            totalPlayers();

            System.out.println(recentRaid);
        } else {
            createRaid(raidCsv);
        }
    }

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

    private Player createPlayer(String[] record){
        return new Player(record[1],record[2],Integer.parseInt(record[3]),Integer.parseInt(record[4]));
    }

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

    private Raid getRecentRaid(){
        Raid recent = null;
        for(int i = 0; i < raids.size()-1; i++){
            recent = raids.get(i).moreRecent(raids.get(i+1));
        }
        return recent;
    }

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
            if(!p.getName().equals(recentRaid.getPlayers().getPlayerById(p.getId()).getName())){
                p.setName(recentRaid.getPlayers().getPlayerById(p.getId()).getName());
            }
            if(recentRaid.getPlayers().containsId(p.getId())){
                activePlayers.addPlayer(p);
            }
        }
    }

    private void updatePlayer(Player p){
        Player listedPlayer = players.getPlayerById(p.getId());
        listedPlayer.addAttacks(p.getAttacks());
        listedPlayer.addDamage(p.getDamage());
        listedPlayer.setName(p.getName());
    }

}
