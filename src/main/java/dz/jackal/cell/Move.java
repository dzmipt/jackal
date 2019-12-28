package dz.jackal.cell;

import dz.jackal.Loc;

public enum Move {

    NW(-1,-1),
    N(-1,0),
    NE(-1,1),
    E(0,1),
    SE(1,1),
    S(1,0),
    SW(1,-1),
    W(0,-1);


    private int dr;
    private int dc;
    Move(int dr, int dc) {
        this.dr = dr;
        this.dc = dc;
    }
    public int index() {
        return ordinal()+1;
    }
    public Move rotate() {
        return Move.values()[(ordinal()+2) % Move.values().length];
    }
    public Loc move(Loc loc) {
        return loc.add(dr, dc);
    }
}
