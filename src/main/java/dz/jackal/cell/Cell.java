package dz.jackal.cell;

import dz.jackal.Hero;
import dz.jackal.Icon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cell implements Serializable {
    private final static long serialVersionUID = 1;

    private Icon icon;
    private boolean closed = true;

    private int count;
    private Set<Hero>[] heroes;
    private int[] gold;
    private int rum = 0;

    public Cell(Icon icon) {
        this(icon,1);
    }

    @SuppressWarnings("unchecked")
    public Cell(Icon icon,int count) {
        this.icon = icon;
        this.count = count;
        heroes = new Set[count];
        gold = new int[count];
        for (int i=0; i<count; i++) {
            heroes[i] = new HashSet<>();
            gold[i] = 0;
        }
    }

    public String getIconView() {
        if (closed) return Icon.CLOSED.getLocation();
        return icon.getLocation();
    }

    public boolean sea() {
        return icon == Icon.SEA;
    }

    public boolean woman() {
        return icon == Icon.WOMAN;
    }

    public boolean fort() {
        return icon == Icon.FORT;
    }

    public boolean balloon() {
        return icon == Icon.BALLOON;
    }

    public boolean cannon() {
        return false;
    }

    public boolean ship() {
        return false;
    }

    public boolean land() {
        return ! (sea() || ship());
    }

    public boolean multiStep() {return count>1;}

    public boolean move() {return false;}

    public int count() {return count;}

    public void open() {
        closed = false;
    }

    public boolean closed() {
        return closed;
    }

    public int gold(int index) {
        return gold[index];
    }

    public void addGold(int index) {
        gold[index]++;
    }
    public void takeGold(int index) {
        gold[index]--;
    }

    public int countRum() {return rum;}
    public void takeRum() {rum = 0;}
    public void setRum(int rum) {this.rum = rum;}

    public List<Hero> heroes(int index) {
        return new ArrayList<>(heroes[index]);
    }

    public void addHero(int index, Hero hero) {
        heroes[index].add(hero);
    }

    public boolean removeHero(int index, Hero hero) {
        return heroes[index].remove(hero);
    }

    public int index(Hero hero) {
        for (int i=0; i<count; i++) {
            if (heroes[i].contains(hero)) return i;
        }
        throw new IllegalArgumentException(hero + " not found at cell " + icon);
    }
}
