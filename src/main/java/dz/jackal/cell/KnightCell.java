package dz.jackal.cell;

import dz.jackal.Icon;
import dz.jackal.Loc;

public class KnightCell extends MoveCell {

    public KnightCell() {
        super(Icon.KNIGHT);
    }

    @Override
    public Loc[] nextSteps(Loc prevLoc, Loc curLoc) {
        Loc[] locs = new Loc[8];
        int index = 0;
        for(int dr = -2; dr<=2; dr++) {
            for (int dc = -2; dc<=2; dc++) {
                if (dr*dc == 0) continue;
                if (Math.abs(dr) == Math.abs(dc)) continue;
                locs[index++] = curLoc.add(dr,dc);
            }
        }
        return locs;
    }

    @Override
    public MoveCell duplicate() {
        return new KnightCell();
    }
}
