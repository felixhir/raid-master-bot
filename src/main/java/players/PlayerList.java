package players;

import java.util.LinkedList;
import java.util.List;

public class PlayerList extends LinkedList<Player> {

    public boolean containsPlayerById(String id){
        for (Player player : this) {
            if (player.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean add(Player player){
        if(this.containsPlayerById(player.getId())) {
            Player editPlayer = getPlayerById(player.getId());
            editPlayer.addAttacks(player.getAttacks());
            editPlayer.setRealName(player.getRealName());
            editPlayer.addDamage(player.getDamage());
            return true;
        } else {
            return super.add(player);
        }
    }

    public Player getPlayerById(String id){
        for(Player p: this){
            if(p.getId().equals(id)){
                return p;
            }
        }
        return null;
    }

    public Player getPlayerByName(String name){
        for(Player p: this){
            if(p.getRealName().equals(name)){
                return p;
            }
        }
        return null;
    }

    @Override
    public String toString(){
        StringBuilder returnString = new StringBuilder("List of players:");
        for(Player p: this){
            returnString.append(" '").append(p.getRealName()).append("',");
        }
        return returnString.substring(0,returnString.length()-1);
    }

    /**
     * Determines the 5 players with the highest damage per attack (dpa)
     * @return PlayerList with 5 highest ranked players
     */
    public List<Player> getTopPlayers(){
        LinkedList<Player> list = new LinkedList<>();
        for(Player p: this){
            for(int i = 0; i < list.size(); i++){
                if(p.getDpa() > list.get(i).getDpa()){
                    list.add(i, p);
                    break;
                }
            }
            if(!list.contains(p)){
                list.add(p);
            }
        }
        return list.subList(0,Math.min(5, list.size()));
    }

}
