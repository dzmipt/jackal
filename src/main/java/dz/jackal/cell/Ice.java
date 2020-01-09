package dz.jackal.cell;

import dz.jackal.Icon;
import dz.jackal.Loc;

public class Ice extends MoveCell {
    private final static long serialVersionUID = 1;

    public Ice() {
        super(Icon.ICE);
    }

    @Override
    public Loc[] nextSteps(Loc prevLoc, Loc curLoc) {
        return new Loc[] {curLoc.add(curLoc.row() - prevLoc.row(), curLoc.col() - prevLoc.col())};
    }

}
