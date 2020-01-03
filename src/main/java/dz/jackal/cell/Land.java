package dz.jackal.cell;

import dz.jackal.Icon;

import java.util.Random;

public class Land extends Cell {
    private final static long serialVersionUID = 1;
    private final static int COUNT = 4;
    private String iconView;

    public Land(Random random) {
        super(Icon.LAND);

        iconView = Icon.LAND.getLocation() + (random.nextInt(COUNT)+1);
    }

    @Override
    public String getIconView() {
        if (temporaryIcon() || closed()) return super.getIconView();

        return iconView;
    }

}
