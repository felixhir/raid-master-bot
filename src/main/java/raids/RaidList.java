package raids;

import java.util.LinkedList;

public class RaidList extends LinkedList<Raid> {

    private final LinkedList<Raid> raids;
    private Raid recentRaid;

    public RaidList(){
        raids = new LinkedList<>();
    }

    public LinkedList<Raid> getList(){
        return this.raids;
    }

    public int size(){
        return this.raids.size();
    }

    @Override
    public Raid get(int index){
        return this.raids.get(index);
    }

    @Override
    public boolean add(Raid raid){
        if(raids.isEmpty()) {
            recentRaid = raid;
            return this.raids.add(raid);
        } else if(raid.getDate().after(getRecentRaid().getDate())) {
            recentRaid = raid;
            return this.raids.add(raid);
        } else {
            return this.raids.add(raid);
        }
    }

    @Override
    public void add(int index, Raid raid) {
        this.raids.add(index, raid);
    }


    public String toString(){
        return "Raid 0: " +
                raids.get(0) +
                " to " +
                raids.get(raids.size()-1);
    }

    public Raid getRecentRaid(){
        return this.recentRaid;
    }
}
