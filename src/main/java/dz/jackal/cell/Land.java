package dz.jackal.cell;

import dz.jackal.Game;
import dz.jackal.Icon;

public class Land extends Cell {
    private final static long serialVersionUID = 1;
    private final static int COUNT = 4;
    private String iconView;

    public Land() {
        super(Icon.LAND);
        random();
    }

    @Override
    public void random() {
        iconView = Icon.LAND.getLocation() + (Game.random.nextInt(COUNT)+1);
    }

    @Override
    public String getIconView() {
        if (temporaryIcon() || closed()) return super.getIconView();

        return iconView;
    }

}
