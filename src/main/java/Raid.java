import java.util.LinkedList;

public class Raid {

    private LinkedList<Player> players;
    private int stage;
    private int tier;
    private int tries;

    public Raid(int tier, int stage, int tries){
        players = new LinkedList<>();
        this.tier = tier;
        this.stage = stage;
        this.tries = tries;
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public String toString(){
        return "Raid (Tier " + this.tier + " - Stage " + this.stage + ") attempt #" + this.tries + " beaten with " + this.players.size() + " players";
    }
}
