package dz.jackal;

public class Missioner extends Hero {
    private final static long serialVersionUID = 1;

    private boolean pirate = false;

    public Missioner(Loc loc) {
        super(HeroId.MISSIONER_ID, loc);
    }

    public void drinkToPirate() {
        pirate = true;
    }

    @Override
    public boolean missioner() {
        return !pirate;
    }
}
