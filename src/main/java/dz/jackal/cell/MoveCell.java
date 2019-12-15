package dz.jackal.cell;

import dz.jackal.Icon;
import dz.jackal.Loc;

abstract public class MoveCell extends Cell {

    public MoveCell(Icon icon) {
        super(icon);
    }

    abstract public Loc[] nextSteps(Loc prevLoc, Loc curLoc);

    @Override
    public boolean move() {
        return true;
    }

    abstract public MoveCell duplicate();
}
