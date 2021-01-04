package objects;

import java.util.LinkedList;

public class RaidList extends LinkedList<Raid> {

    private final LinkedList<Raid> raids;

    public RaidList(){
        raids = new LinkedList<>();
    }

    public LinkedList<Raid> getList(){
        return this.raids;
    }

    public int size(){
        return this.raids.size();
    }

    public Raid get(int index){
        return this.raids.get(index);
    }

    public void addRaid(Raid raid){
        this.raids.add(raid);
    }
}
