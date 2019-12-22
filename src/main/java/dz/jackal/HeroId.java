package dz.jackal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeroId implements Serializable {
    private final static long serialVersionUID = 2;

    public final static HeroId BENGUNN_ID = new HeroId(4,0);
    public final static HeroId FRIDAY_ID = new HeroId(5,0);
    public final static HeroId MISSIONER_ID = new HeroId(6,0);

    public final static List<HeroId> ALL;
    private int group,num;

    private HeroId(int group, int num) {
        this.group = group;
        this.num = num;
    }

    public int group() {return group;}
    public int num() {return num;}

    public boolean benGunn() {return this == BENGUNN_ID;}
    public boolean friday() {return this == FRIDAY_ID;}
    public boolean missioner() {return this == MISSIONER_ID;}
    public boolean pirate() {return group<4;}

    @Override
    public boolean equals(Object obj) {
        HeroId heroId = (HeroId) obj;
        return heroId.num == num && heroId.group == group;
    }

    @Override
    public int hashCode() {
        return group *3+num;
    }


    public String toString() {
        if (benGunn()) return "Ben Gunn";
        if (friday()) return "Friday";
        if (missioner()) return "Missioner";

        return "team: " + group +"; num: " + num;
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
