package dz.jackal.cell;

import dz.jackal.Icon;

public class Ship extends Cell {
    private final static long serialVersionUID = 1;

    private int team;
    public Ship(int team) {
        super(Icon.SHIP);
        this.team = team;
    }

    @Override
    public boolean ship() {
        return true;
    }

    public int team() {return team;}
}
