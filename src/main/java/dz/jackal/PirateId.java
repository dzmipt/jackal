package dz.jackal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PirateId implements Serializable {
    private final static long serialVersionUID = 1;

    public final static List<PirateId> ALL;
    private int team,num;
    public PirateId(int team, int num) {
        this.team = team;
        this.num = num;
    }
    public int getTeam() {return team;}
    public int getNum() {return num;}

    public int team() {return team;}
    public int num() {return num;}

    @Override
    public boolean equals(Object obj) {
        PirateId pirateId = (PirateId) obj;
        return pirateId.num == num && pirateId.team == team;
    }

    @Override
    public int hashCode() {
        return team*3+num;
    }


    public String toString() {
        return "team: " + team +"; num: " + num;
    }

    static {
        List<PirateId> all = new ArrayList<>();
        for (int team=0;team<4;team++){
            for (int num=0;num<3;num++){
                all.add(new PirateId(team,num));
            }
        }
        ALL = Collections.unmodifiableList(all);
    }
}
