package dz.jackal;

import java.io.Serializable;

public class Hero implements Serializable {
    private final static long serialVersionUID = 2;

    private HeroId id;
    private int team;
    private Loc loc;
    private Loc initStepLoc, prevLoc;
    private boolean dead = false;
    private boolean drunk = false;
    public Hero(HeroId id, int team, Loc loc) {
        this.id = id;
        this.team = team;
        this.loc = loc;
    }
    public Loc getLoc() {
        return loc;
    }

    public void setLoc(Loc loc) {
        this.loc = loc;
    }

    public boolean dead() {return dead;}

    public void die() {
        dead = true;
        loc = null;
    }

    public void setDrunk(boolean drunk) {
        this.drunk = drunk;
    }
    public boolean drunk() {return drunk;}

    public Loc getInitStepLoc() {
        return initStepLoc;
    }

    public void setInitStepLoc(Loc initStepLoc) {
        this.initStepLoc = initStepLoc;
    }

    public Loc getPrevLoc() {
        return prevLoc;
    }

    public void setPrevLoc(Loc prevLoc) {
        this.prevLoc = prevLoc;
    }

    public HeroId id() {return id;}

    public boolean benGunn() {return id.benGunn();}
    public boolean friday() {return id.friday();}
    public boolean missioner() {return id.missioner() && !drunk;}
    public boolean pirate() {return id.pirate() || benGunn() || (id.missioner() && drunk);}

    public int team() {return team;}
    public void setTeam(int team) {this.team = team;}

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Hero p = (Hero) obj;
        return id.equals(p.id);
    }

    @Override
    public String toString() {
        return id + " @ " + loc;
    }
}
