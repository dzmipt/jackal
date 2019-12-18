package dz.jackal;

import java.io.Serializable;

public class Pirate implements Serializable {
    private final static long serialVersionUID = 1;

    private PirateId id;
    private Loc loc;
    private Loc initStepLoc, prevLoc;
    private boolean dead = false;
    public Pirate(PirateId id, Loc loc) {
        this.id = id;
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

    public PirateId id() {return id;}

    public int team() {return id.team();}

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Pirate p = (Pirate) obj;
        return id.equals(p.id);
    }
}
