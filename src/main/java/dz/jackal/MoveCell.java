package dz.jackal;

abstract public class MoveCell extends Cell {

    public MoveCell(Icon icon) {
        super(icon);
    }

    abstract public Loc[] nextSteps(Loc prevLoc, Loc curLoc);

    @Override
    public boolean move() {
        return true;
    }

    abstract MoveCell duplicate();
}
