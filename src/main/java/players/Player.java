package players;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;

public class Player {

    private String byteName;
    private String realName;
    private final String id;
    private int attacks;
    private int damage;

    public Player(String byteName, String id, int attacks, int damage){
        this.byteName = byteName;
        this.realName = this.byteArrayToString(byteName);
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

    public String getRealName(){
        return this.realName;
    }

    public String getByteName() {
        return this.byteName;
    }

    public void setRealName(String name) {
        this.realName = name;
        this.byteName = Arrays.toString(name.getBytes(StandardCharsets.UTF_8));
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
        DecimalFormat format = new DecimalFormat("#,###");
        return this.getRealName() + " (" + this.id + ") has dealt " + format.format(this.damage) + " damage with " + this.attacks +" attacks (" + format.format(this.getDpa()) + " DpA).";
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

    public String byteArrayToString(String byteString) {
        byteString = byteString.substring(1, byteString.length()-1).replace(" ", "");
        String[] arr = byteString.split(",");
        byte[] bytes = new byte[arr.length];
        for(int i = 0; i < arr.length; i++) {
            bytes[i] = (byte) Integer.parseInt(arr[i]);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
