package dz.jackal.cell;

import dz.jackal.Icon;

public class Cave extends Cell {
    private final static long serialVersionUID = 1;

    private static int nextExit = 0;

    private int exit;

    public Cave() {
        super(Icon.CAVE);
        this.exit = nextExit++;
    }

    @Override
    public boolean cave() {
        return true;
    }

    public int getExit() {
        return exit;
    }
}
