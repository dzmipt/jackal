package dz.jackal.cell;

import dz.jackal.Icon;
import dz.jackal.Pirate;

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
    private Set<Pirate>[] heroes;
    private int[] gold;

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

    public List<Pirate> heroes(int index) {
        return new ArrayList<>(heroes[index]);
    }

    public void addHero(int index, Pirate pirate) {
        heroes[index].add(pirate);
    }

    public boolean removeHero(int index, Pirate pirate) {
        return heroes[index].remove(pirate);
    }

    public int index(Pirate pirate) {
        for (int i=0; i<count; i++) {
            if (heroes[i].contains(pirate)) return i;
        }
        throw new IllegalArgumentException();
    }
}
