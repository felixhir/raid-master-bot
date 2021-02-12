package raids;

import java.util.LinkedList;

public class RaidList extends LinkedList<Raid> {

    @Override
    public boolean add(Raid raid){
        if(this.isEmpty()) {
            return super.add(raid);
        }
        for(int i = 0; i < this.size(); i++){
            if(raid.getTier() < this.get(i).getTier()) {
                this.add(i, raid);
                return true;
            } else if(raid.getTier() == this.get(i).getTier()) {
                if(raid.getStage() < this.get(i).getStage()) {
                    this.add(i, raid);
                    return true;
                } else {
                    if(raid.getStage() == this.get(i).getStage()) {
                        if(raid.getTries() < this.get(i).getTries()) {
                            this.add(i, raid);
                            return true;
                        }
                    }
                }
            }
        }
        return super.add(raid);
    }

    public String toString(){
        StringBuilder s = new StringBuilder("List contains:");
        for(Raid r: this) {
            s.append(" '").append(r.getName()).append("',");
        }
        s = new StringBuilder(s.substring(0, s.length() - 1));

        return s.toString();
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
