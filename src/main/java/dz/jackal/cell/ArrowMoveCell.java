package dz.jackal.cell;

import dz.jackal.Icon;
import dz.jackal.Loc;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrowMoveCell extends MoveCell {
    private final static long serialVersionUID = 1;

    private Move[] moves;
    private String iconView;
    private Random random;

    public ArrowMoveCell(Random random, Move ... m ){
        super(Icon.MOVE);
        this.random = random;

        moves = new Move[m.length];
        int count = random.nextInt(4);
        for(int index=0; index<m.length; index++) {
            moves[index] = m[index];
            for (int i=0;i<=count;i++) {
                moves[index] = moves[index].rotate();
            }
        }
        initIconView();
    }

    public void setMoves(Move ... m) {
        this.moves = m;
        initIconView();
    }

    private void initIconView() {
        iconView = Icon.MOVE.getLocation() +
                Stream.of(moves)
                        .map(d->""+(d.index()))
                        .sorted()
                        .collect(Collectors.joining());
    }

    @Override
    public String getIconView() {
        if (closed()) {
            return super.getIconView();
        }
        else {
            return iconView;
        }
    }

    @Override
    public MoveCell duplicate() {
        return new ArrowMoveCell(random, moves);
    }

    @Override
    public Loc[] nextSteps(Loc prevLoc, Loc curLoc) {
        return Stream.of(moves).map(m->m.move(curLoc)).toArray(Loc[]::new);
    }
}
