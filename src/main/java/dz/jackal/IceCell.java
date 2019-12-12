package dz.jackal;

public class IceCell extends MoveCell {

    public IceCell() {
        super(Icon.ICE);
    }

    @Override
    public Loc[] nextSteps(Loc prevLoc, Loc curLoc) {
        return new Loc[] {curLoc.add(curLoc.row() - prevLoc.row(), curLoc.col() - prevLoc.col())};
    }

    @Override
    public MoveCell duplicate() {
        return new IceCell();
    }
}
