import java.util.LinkedList;

public class Raid {

    private LinkedList<Player> players;
    private int stage;
    private int tier;

    public Raid(int tier, int stage){
        players = new LinkedList<>();
        this.tier = tier;
        this.stage = stage;
    }

    public void addPlayer(Player player){
        players.add(player);
    }
}
