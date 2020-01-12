package dz.jackal.cell;

import dz.jackal.Icon;

public class TeeHee extends Land {
    private final static long serialVersionUID = 1;

    private boolean teeHee = true;

    public TeeHee() {
        super();
    }

    @Override
    public boolean teeHee() {
        return teeHee;
    }

    public void activate() {
        teeHee = false;
        setTempIcon(Icon.TEEHEE);
    }
}
