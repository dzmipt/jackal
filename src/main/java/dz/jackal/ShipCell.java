package dz.jackal;

public class ShipCell extends Cell {
    private int team;
    public ShipCell(int team) {
        super(Icon.SHIP);
        this.team = team;
    }

    @Override
    public boolean ship() {
        return true;
    }

    public int team() {return team;}
}
