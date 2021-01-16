package objects;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

public class Player {

    private byte[] name;
    private final String id;
    private int attacks;
    private int damage;
    private int participations;
    private final RaidList raids;

    public Player(String name, String id, int attacks, int damage){
        this.name = name.getBytes(StandardCharsets.UTF_8);
        this.id = id;
        this.attacks = attacks;
        this.damage = damage;
        this.raids = new RaidList();
        this.participations = 1;
    }

    public void addDamage(int dmg){
        this.damage += dmg;
    }

    public void addAttacks(int attacks) {
        this.attacks += attacks;
    }

    public void addParticipation(){
        this.participations++;
    }

    public void addRaid(Raid raid){
        this.raids.addRaid(raid);
    }

    public RaidList getRaids(){
        return this.raids;
    }

    public int getParticipations(){
        return this.participations;
    }

    public byte[] getName(){
        return this.name;
    }

    public String getNameAsString(){
        return new String(name, StandardCharsets.UTF_8);
    }

    public void setName(String name) {
        this.name = name.getBytes(StandardCharsets.UTF_8);
    }

    public String getId(){
        return this.id;
    }

    public int getAttacks(){
        return this.attacks;
    }

    public int getDamage(){
        return this.damage;
    }

    public String toString(){
        DecimalFormat format = new DecimalFormat("#");
        return this.getNameAsString() + " (" + this.id + ") has dealt " + this.damage + " with " + this.attacks +" attacks (" + format.format(this.getDpa()) + " DpA).";
    }

    /**
     * evaluates the damage per attack (dpa) of a player
     * @return the dpa
     */
    public double getDpa(){
        double dpa = 0;
        try{
            //noinspection IntegerDivisionInFloatingPointContext
            dpa = this.damage / this.attacks;
        } catch (ArithmeticException ignored){
        }
        return dpa;
    }
}
