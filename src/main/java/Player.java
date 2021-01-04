import java.text.DecimalFormat;
import java.util.LinkedList;

public class Player {

    private String name;
    private final String id;
    private int attacks;
    private int damage;
    private int participations;
    private LinkedList<Raid> raids;

    public Player(String name, String id, int attacks, int damage, Raid raid){
        this.name = name;
        this.id = id;
        this.attacks = attacks;
        this.damage = damage;
        this.raids = new LinkedList<>();
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
        this.raids.add(raid);
    }

    public LinkedList<Raid> getRaids(){
        return this.raids;
    }

    public int getParticipations(){
        return this.participations;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
        return this.name + " (" + this.id + ") has dealt " + this.damage + " with " + this.attacks +" attacks (" + format.format(this.getDpa()) + " DpA).";
    }

    public Raid getLatestRaid(){
        Raid recent = raids.get(0);
        for(int i = 0; i < raids.size() - 1; i++){
            recent = recent.moreRecent(raids.get(i+1));
        }
        return recent;
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
