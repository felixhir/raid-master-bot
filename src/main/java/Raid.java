public class Raid {

    PlayerList players;
    private final int stage;
    private final int tier;
    private final int tries;

    public Raid(int tier, int stage, int tries){
        players = new PlayerList();
        this.tier = tier;
        this.stage = stage;
        this.tries = tries;
    }

    public void addPlayer(Player player){
        players.addPlayer(player);
    }

    public String toString(){
        return "Raid (Tier " + this.tier + " - Stage " + this.stage + ") attempt #" + this.tries + " beaten with " + this.players.size() + " players";
    }

    public int getStage(){
        return this.stage;
    }

    public int getTier(){
        return this.tier;
    }

    public int getTries(){
        return this.tries;
    }

    public PlayerList getPlayers(){
        return this.players;
    }

    //returns the more recent raid of 2 raids
    public Raid moreRecent(Raid r){
        Raid raid;
        if (this.getTier() > r.getTier()){
            raid = this;
        } else if(this.getTier() == r.getTier()){
            if (this.getStage() > r.getStage()){
                raid = this;
            } else if(this.getStage() == r.getStage()){
                if(this.getTries() > r.getTries()){
                    raid = this;
                } else {
                    raid = r;
                }
            } else {
                raid = r;
            }
        } else {
            raid = r;
        }
        return raid;
    }
}
