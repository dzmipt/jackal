package dz.jackal.cell;

import dz.jackal.Icon;
import dz.jackal.Loc;

import java.util.Random;

public class Cannon extends Cell {
    private final static long serialVersionUID = 1;

    private Move move;
    private String iconView;

    public Cannon(Random random) {
        super(Icon.CANNON);

        move = Move.N;
        int count = random.nextInt(4);
        for (int i=0;i<=count;i++) move = move.rotate();
        initIconView();
    }

    public void setMove(Move move) {
        this.move = move;
        initIconView();
    }

    private void initIconView() {
        iconView = Icon.CANNON.getLocation() + move.index();
    }

    @Override
    public boolean cannon() {
        return true;
    }

    @Override
    public String getIconView() {
        if (closed()) {
            return super.getIconView();
        } else {
            return iconView;
        }
    }

    public Loc fire(Loc loc) {
        while ( loc.row()>0 && loc.row()<12 && loc.col()>0 && loc.col()<12 ) {
            loc = move.move(loc);
        }
        return loc;
    }

}
