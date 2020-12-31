public class Player {

    private String name;
    private String id;
    private int attacks;
    private int damage;

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
        return this.name + " (" + this.id + ") has dealt " + this.damage + " with " + this.attacks +" attacks.";
    }
}
