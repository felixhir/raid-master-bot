package raids;

import players.Player;
import players.PlayerList;

import java.sql.Date;
import java.text.DecimalFormat;

public class Raid {

    PlayerList players;
    private final int stage;
    private final int tier;
    private final int tries;
    private final String name;
    private final String clanName;
    private final Date date;
    private int maxAttacks;
    private int totalDamage;
    private int totalAttacks;

    public Raid(int tier, int stage, int tries, String clan, Date date){
        this.date = date;
        this.clanName = clan;
        players = new PlayerList();
        this.tier = tier;
        this.stage = stage;
        this.tries = tries;
        this.maxAttacks = 0;
        this.totalAttacks = 0;
        this.totalDamage = 0;
        this.name = tier + String.format("%02d", stage) + String.format("%02d", tries) + this.clanName.substring(0,5);
    }

    public String getName() {
        return name;
    }

    public String getClanName() {
        return clanName;
    }

    public Date getDate() {
        return date;
    }

    public void addPlayer(Player player){
        players.add(player);
        if(player.getAttacks() > maxAttacks) maxAttacks = player.getAttacks();
        totalDamage+= player.getDamage();
        totalAttacks+= player.getAttacks();
    }

    public int getMaxAttacks() {
        return this.maxAttacks;
    }

    public int getTotalDamage() {
        return this.totalDamage;
    }

    public int getTotalAttacks() {
        return totalAttacks;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public double getDpa(){
        return totalDamage / totalAttacks;
    }

    public String toString(){
        return "Tier " + this.tier + " - Stage " + this.stage + " - attempt #" + this.tries;
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

    public int getMissedAttacks() {
        int missed = 0;
        for(Player p: players) {
            missed+= this.maxAttacks - p.getAttacks();
        }
        return missed;
    }

    public double getPotential() {
        return getMissedAttacks() / (this.maxAttacks * players.size() * 1.0) * 100;
    }

    public int getFewestAttacksNeeded() {
        int neededAttacks = (int) (this.totalDamage / this.getDpa());

        return neededAttacks / this.players.size();
    }

    public String factify() {
        DecimalFormat df = new DecimalFormat("##.#");
        int minAttacks = (int) (this.getTotalDamage() / (this.getDpa() * this.players.size()));
        int timeSave = ((this.getMaxAttacks() - minAttacks) / 4) * 12;

        return "You used " + df.format(100 - this.getPotential()) + "% of your attacks," +
                " if everyone attacked " + minAttacks + " times you could have saved " + timeSave + " hours.";
    }
}
