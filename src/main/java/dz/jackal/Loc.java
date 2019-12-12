package dz.jackal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Loc {
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

    @Override
    public boolean equals(Object obj) {
        Loc loc = (Loc) obj;
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