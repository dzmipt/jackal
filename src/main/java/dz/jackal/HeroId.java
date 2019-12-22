package dz.jackal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeroId implements Serializable {
    private final static long serialVersionUID = 2;

    public final static HeroId BENGUNN_ID = new HeroId(-1,3);
    public final static HeroId FRIDAY_ID = new HeroId(-1,4);
    public final static HeroId MISSIONER_ID = new HeroId(-1,5);

    public final static List<HeroId> ALL;
    private int team,num;

    private HeroId(int team, int num) {
        this.team = team;
        this.num = num;
    }

    public int team() {return team;}
    //public int num() {return num;}

    public boolean benGunn() {return this == BENGUNN_ID;}
    public boolean friday() {return this == FRIDAY_ID;}
    public boolean missioner() {return this == MISSIONER_ID;}
    public boolean pirate() {return team != -1;}

    @Override
    public boolean equals(Object obj) {
        HeroId heroId = (HeroId) obj;
        return heroId.num == num && heroId.team == team;
    }

    @Override
    public int hashCode() {
        return team *30+num;
    }


    public String toString() {
        if (benGunn()) return "Ben Gunn";
        if (friday()) return "Friday";
        if (missioner()) return "Missioner";

        return "team: " + team +"; num: " + num;
    }

    static {
        List<HeroId> all = new ArrayList<>();
        for (int team=0;team<4;team++){
            for (int num=0;num<3;num++){
                all.add(new HeroId(team,num));
            }
        }
        all.add(BENGUNN_ID);
        all.add(FRIDAY_ID);
        all.add(MISSIONER_ID);
        ALL = Collections.unmodifiableList(all);
    }
}
