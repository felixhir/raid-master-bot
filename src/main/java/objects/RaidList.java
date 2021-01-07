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

    public void addRaid(int index, Raid raid) {
        this.raids.add(index, raid);
    }

    public Raid getMostRecentRaid(){
        Raid recent = raids.get(0);
        for(int i = 0; i < raids.size() - 1; i++){
            recent = recent.moreRecent(raids.get(i+1));
        }
        return recent;
    }

    public RaidList sort(){
        RaidList list = new RaidList();
        list.addRaid(raids.get(0));

        for(int i = 1; i < raids.size(); i++){
            for(int j = 0; i < list.size(); j++){
                if(raids.get(i).getTier() < list.get(j).getTier()){
                    list.addRaid(j,raids.get(i));
                    break;
                } else if(raids.get(i).getTier() == list.get(j).getTier()) {
                    if(raids.get(i).getStage() < list.get(j).getStage()) {
                        list.addRaid(j, raids.get(i));
                        break;
                    } else if(raids.get(i).getStage() == list.get(j).getStage()) {
                        if(raids.get(i).getTries() < list.get(j).getTries()) {
                            list.addRaid(j, raids.get(i));
                            break;
                        }
                    }
                }
            }
            list.addRaid(raids.get(i));
        }
        return list;
    }

    public String toString(){
        return "Raid 0: " +
                raids.get(0) +
                " to " +
                raids.get(raids.size()-1);
    }
}
