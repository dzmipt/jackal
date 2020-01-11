package dz.jackal.cell;

import dz.jackal.Icon;

public class Den extends Land {
    private final static long serialVersionUID = 1;

    private boolean den = true;

    public Den() {
        super();
    }

    @Override
    public boolean den() {
        return den;
    }

    public void activate() {
        den = false;
        setTempIcon(Icon.DEN);
    }
}
