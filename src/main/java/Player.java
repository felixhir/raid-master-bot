import java.text.DecimalFormat;

public class Player {

    private String name;
    private final String id;
    private int attacks;
    private int damage;
    private Raid latestRaid;

    public Player(String name, String id, int attacks, int damage){
        this.name = name;
        this.id = id;
        this.attacks = attacks;
        this.damage = damage;
    }

    public void addDamage(int dmg){
        this.damage += dmg;
    }

    public void addAttacks(int attacks) {
        this.attacks += attacks;
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

    public void setLatestRaid(Raid r){
        this.latestRaid = r;
    }

    public Raid getLatestRaid(){
        return this.latestRaid;
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
