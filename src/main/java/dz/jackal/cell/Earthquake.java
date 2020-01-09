package dz.jackal.cell;

import dz.jackal.Icon;

public class Earthquake extends Land {
    private final static long serialVersionUID = 1;

    private boolean earthquake = true;

    public Earthquake() {
        super();
    }

    @Override
    public boolean earthquake() {
        return earthquake;
    }

    public void activate() {
        earthquake = false;
        setTempIcon(Icon.EARTHQUAKE);
    }
}
