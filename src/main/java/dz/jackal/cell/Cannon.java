package dz.jackal.cell;

import dz.jackal.Game;
import dz.jackal.Icon;
import dz.jackal.Loc;

public class Cannon extends Cell {
    private final static long serialVersionUID = 1;

    private Move move;
    private String iconView;

    public Cannon() {
        super(Icon.CANNON);
        move = Move.N;
        random();
    }

    @Override
    public void random() {
        int count = Game.random.nextInt(4);
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
        return move.move(loc);
    }

}
