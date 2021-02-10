package raids;

import java.util.LinkedList;

public class RaidList extends LinkedList<Raid> {

    private Raid recentRaid;

    @Override
    public boolean add(Raid raid){
        if(this.isEmpty()) {
            recentRaid = raid;
            return super.add(raid);
        } else if(raid.getDate().after(getRecentRaid().getDate())) {
            recentRaid = raid;
            return super.add(raid);
        } else {
            for(int i = 0; i < this.size(); i++) {
                if(raid.getDate().before(this.get(i).getDate())){
                    this.add(i, raid);
                    return true;
                }
            }
            return true;
        }
    }

    public String toString(){
        StringBuilder s = new StringBuilder("List contains:");
        for(Raid r: this) {
            s.append(" '").append(r.getName()).append("',");
        }
        s = new StringBuilder(s.substring(0, s.length() - 1));

        return s.toString();
    }

    public Raid getRecentRaid(){
        return this.recentRaid;
    }

    public boolean containsRaid(String name) {
        for(Raid r: this) {
            if (r.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
