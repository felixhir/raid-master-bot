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

    public String toString(){
        StringBuilder s = new StringBuilder("List contains:");
        for(Raid r: raids) {
            s.append(" '").append(r.getName()).append("',");
        }
        s = new StringBuilder(s.substring(0, s.length() - 1));

        return s.toString();
    }

    public Raid getRecentRaid(){
        return this.recentRaid;
    }
}
