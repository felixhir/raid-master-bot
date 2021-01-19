package objects;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class PlayerList extends LinkedList<Player> {

    private final LinkedList<Player> players;

    public PlayerList(){
        players = new LinkedList<>();
    }

    public boolean containsPlayerById(String id){
        for (Player player : players) {
            if (player.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsName(String name){
        System.out.println("CALLED!");
        System.out.println(players.size());
        for (Player player : players) {
            System.out.println(player.getRealName());
            if (player.getRealName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean add(Player player){
        if(this.containsPlayerById(player)) {
            Player editPlayer = getPlayerById(player.getId());
            editPlayer.addAttacks(player.getAttacks());
            editPlayer.setRealName(player.getRealName());
            editPlayer.addDamage(player.getDamage());
            return true;
        } else {
            return players.add(player);
        }
    }

    public Player getPlayerById(String id){
        for(Player p: players){
            if(p.getId().equals(id)){
                return p;
            }
        }
        return null;
    }

    public Player getPlayerByName(String name){
        for(Player p: players){
            if(p.getRealName().equals(name)){
                return p;
            }
        }
        return null;
    }

    @Override
    public Player get(int index){
        return players.get(index);
    }

    @Override
    public int size(){
        return players.size();
    }

    public LinkedList<Player> getList(){
        return this.players;
    }

    @Override
    public String toString(){
        StringBuilder returnString = new StringBuilder();
        for(Player p: players){
            returnString.append(p.toString()).append("\n");
        }
        return returnString.toString();
    }

    /**
     * Implements a subList() for PlayerList
     * @param start index of first player
     * @param end index of last player (inclusive)
     * @return list of players from index start to end
     */
    @Override
    public @NotNull PlayerList subList(int start, int end){
        PlayerList list = new PlayerList();
        for(int i = start; i < end+1; i++){
            list.add(players.get(i));
        }
        return list;
    }

    /**
     * Determines the 5 players with the highest damage per attack (dpa)
     * @return PlayerList with 5 highest ranked players
     */
    public PlayerList getTopPlayers(){
        PlayerList list = new PlayerList();
        for(Player p: this.getList()){
            for(int i = 0; i < list.size(); i++){
                if(p.getDpa() > list.get(i).getDpa()){
                    list.getList().add(i, p);
                    break;
                }
            }
            if(!list.getList().contains(p)){
                list.add(p);
            }
        }
        return list.subList(0,4);
    }

    private boolean containsPlayerById(Player player) {
        for(Player p: players) {
            if(p.getId().equals(player.getId())) {
                return true;
            }
        }
        return false;
    }
}
