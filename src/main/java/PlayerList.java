import java.util.LinkedList;

public class PlayerList {

    private LinkedList<Player> players;

    public PlayerList(){
        players = new LinkedList<>();
    }

    public boolean containsId(String id){
        for (Player player : players) {
            if (player.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public Player getPlayerById(String id){
        for(Player p: players){
            if(p.getId().equals(id)){
                return p;
            }
        }
        return null;
    }

    public Player get(int index){
        return players.get(index);
    }

    public int size(){
        return players.size();
    }

    public LinkedList<Player> getList(){
        return this.players;
    }

    public String toString(){
        StringBuilder returnString = new StringBuilder();
        for(Player p: players){
            returnString.append(p.toString()).append("\n");
        }
        return returnString.toString();
    }
}
