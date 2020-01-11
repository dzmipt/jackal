package dz.jackal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Loc implements Serializable {
    private final static long serialVersionUID = 1;

    public final static List<Loc> ALL;
    private int row, col;
    public Loc(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {return row;}
    public int getCol() {return col;}

    public int row() {return row;}
    public int col() {return col;}
    public Loc add(int dRow,int dCol) {
        return new Loc(row+dRow,col+dCol);
    }

    public Stream<Loc> around() {
        return Stream.of(-1,1).flatMap(dx -> Stream.of(add(dx,0),add(0,dx)));
    }
    public Stream<Loc> allAround() {
        return Stream.concat(around(), diagonal());
    }
    public Stream<Loc> diagonal() {
        return Stream.of(-1,1).flatMap(dr -> Stream.of(-1,1).map(dc -> add(dr, dc)));
    }

    public Loc stepTo(Loc newLoc) {
        return add(Integer.signum(newLoc.row-row),
                    Integer.signum(newLoc.col-col));
    }
    public Stream<Loc> path(Loc newLoc) {
        return Stream.iterate(this, l-> l.stepTo(newLoc))
                        .skip(1)
                        .limit(distance(newLoc));
    }

    public boolean diagonal(Loc loc) {
        return Math.abs(loc.row - row) == 1 && Math.abs(loc.col - col) == 1;
    }

    public int distance(Loc loc) {
        int dr = Math.abs(this.row - loc.row);
        int dc = Math.abs(this.col - loc.col);
        return Math.max(dr, dc);
    }

    @Override
    public boolean equals(Object obj) {
        Loc loc = (Loc) obj;
        if (loc == null) return false;
        return loc.row == row && loc.col == col;
    }

    @Override
    public int hashCode() {
        return row*13+col;
    }

    public String toString() {
        return "row: " + row + "; col: " + col;
    }


    static {
        List<Loc> all = new ArrayList<>();
        for(int row=0; row<13; row++) {
            for(int col=0; col<13;col++) {
                all.add(new Loc(row,col));
            }
        }
        ALL = Collections.unmodifiableList(all);
    }
}
