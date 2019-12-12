package dz.jackal;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrowMoveCell extends MoveCell {

    private static Random random = new Random();

    private Move[] moves;
    private String iconView;

    public ArrowMoveCell(Move ... m ){
        super(Icon.MOVE);

        moves = new Move[m.length];
        int count = random.nextInt(4);
        for(int index=0; index<m.length; index++) {
            moves[index] = m[index];
            for (int i=0;i<count;i++) {
                moves[index] = moves[index].rotate();
            }
        }

        iconView = Icon.MOVE.getLocation() +
                            Stream.of(moves)
                                .map(d->""+(d.ordinal()+1))
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
        return new ArrowMoveCell(moves);
    }

    @Override
    public Loc[] nextSteps(Loc prevLoc, Loc curLoc) {
        return Stream.of(moves).map(m->m.move(curLoc)).toArray(Loc[]::new);
    }
}
