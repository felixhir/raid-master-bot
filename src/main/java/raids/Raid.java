package raids;

import players.Player;
import players.PlayerList;

import java.sql.Date;

public class Raid {

    PlayerList players;
    private final int stage;
    private final int tier;
    private final int tries;
    private final String name;
    private final String clanName;
    private final Date date;

    public Raid(int tier, int stage, int tries, String clan, Date date){
        this.date = date;
        this.clanName = clan;
        players = new PlayerList();
        this.tier = tier;
        this.stage = stage;
        this.tries = tries;
        this.name = tier + String.format("%02d", stage) + String.format("%02d", tries) + this.clanName.substring(0,5);
    }

    public String getName() {
        return name;
    }

    public String getClanName() {
        return clanName;
    }

    public Date getDate() {
        return date;
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public String toString(){
        return "Raid (Tier " + this.tier + " - Stage " + this.stage + ") attempt #" + this.tries + " beaten with " + this.players.size() + "players";
    }

    public int getStage(){
        return this.stage;
    }

    public int getTier(){
        return this.tier;
    }

    public int getTries(){
        return this.tries;
    }

    public PlayerList getPlayers(){
        return this.players;
    }

}
