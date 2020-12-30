import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class RaidHandler {
    LinkedList<Player> players;
    LinkedList<Raid> raids;

    public RaidHandler(){
        players = new LinkedList<>();
        raids = new LinkedList<>();
        parseRaids();
    }

    private void parseRaids(){
        File[] files = new File("./parsed").listFiles();

        for(File file: files){
            try(BufferedReader br = Files.newBufferedReader(Paths.get(String.valueOf(file)))) {
                CSVReader csvReader = new CSVReader(br);

                String[] record;
                csvReader.readNext();
                while ((record = csvReader.readNext()) != null){
                    players.add(createPlayer(record));
                }
            } catch (IOException | CsvValidationException e) {
                e.printStackTrace();
            }
        }
        System.out.println(players);
    }

    private Player createPlayer(String[] record){
        return new Player(record[1],record[2],Integer.valueOf(record[3]),Integer.valueOf(record[4]));
    }
}
