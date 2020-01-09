package dz.jackal.cell;

import dz.jackal.Icon;
import dz.jackal.Loc;

abstract public class MoveCell extends Cell {
    private final static long serialVersionUID = 1;
    MoveCell(Icon icon) {
        super(icon);
    }

    abstract public Loc[] nextSteps(Loc prevLoc, Loc curLoc);

    @Override
    public boolean move() {
        return true;
    }

}
