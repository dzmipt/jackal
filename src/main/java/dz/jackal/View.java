package dz.jackal;

import dz.jackal.cell.Cell;

import java.util.*;

public class View {

    public String id;
    public String teamName;
    public CellView[][] cells;
    public List<PirateView> heroes;
    public AnimateShip animateShip = null;
    public AnimateRum animateRum = null;
    public Loc[] ship = new Loc[4];
    public int[] gold = new int[4];
    public int[] rum = new int[4];
    public int currentTeam;
    public int benGunnTeam, fridayTeam, missionerTeam;

    public View(Game game, Map<HeroId,Loc[]> steps, Map<HeroId,Loc[]> stepsWithGold, Set<HeroId> rumReady) {
        id = game.getId();
        teamName = game.getTeamName(game.getCurrentTeam());

        initCellView(game);
        initPirateView(game, steps, stepsWithGold, rumReady);
        currentTeam = game.getCurrentTeam();
        for(int team=0;team<4;team++) {
            gold[team] = game.getTeamGold(team);
            rum[team] = game.getTeamRum(team);
            ship[team] = game.getTeamShipLoc(team);
        }
        benGunnTeam = game.getHero(HeroId.BENGUNN_ID).team();
        fridayTeam = game.getHero(HeroId.FRIDAY_ID).team();
        missionerTeam = game.getHero(HeroId.MISSIONER_ID).team();
    }

    private void initCellView(Game game) {
        cells = new CellView[13][13];
        for(Loc loc:Loc.ALL) {
            Cell cell = game.getCell(loc);
            CellView cellView = new CellView(cell.getIconView());
            if (cell.sea() || cell.closed()) {
                cellView.count = 1;
                cellView.gold = new int[]{0};
            } else {
                int count = cell.count();
                cellView.count = count;
                cellView.gold = new int[count];
                for(int i=0;i<count;i++) {
                    cellView.gold[i] = cell.gold(i);
                }
            }
            cells[loc.row()][loc.col()] = cellView;
        }
    }

    private void initPirateView(Game game, Map<HeroId,Loc[]> steps, Map<HeroId,Loc[]> stepsWithGold, Set<HeroId> rumReady) {
        heroes = new ArrayList<>();
        for(HeroId id: HeroId.ALL) {
            PirateView pirateView = new PirateView();
            Hero hero = game.getHero(id);
            Loc loc = hero.getLoc();

            if (hero.id().equals(HeroId.MISSIONER_ID) && !hero.missioner() ) {
                pirateView.notes.add(PirateView.Note.pirate);
            }
            if (hero.drunk()) pirateView.notes.add(PirateView.Note.drunk);

            if (hero.trapped()) {
                pirateView.notes.add(PirateView.Note.trapped);
            }

            pirateView.dead = hero.dead();
            pirateView.hidden = hero.team() == -1;
            if (!pirateView.dead && !pirateView.hidden) {
                pirateView.loc = loc;
                pirateView.index = game.getCell(loc).index(hero);
            }

            pirateView.steps = steps.get(id);
            pirateView.stepsWithGold = stepsWithGold.get(id);
            pirateView.rumReady = rumReady.contains(id);

            heroes.add(pirateView);
        }

    }

    public View setAnimateShip(AnimateShip animateShip) {
        this.animateShip = animateShip;
        return this;
    }

    public View setAnimateRum(AnimateRum animateRum) {
        this.animateRum = animateRum;
        return this;
    }

    public View setViaLoc(HeroId id, Loc viaLoc) {
        int index = HeroId.ALL.indexOf(id);
        heroes.get(index).viaLoc = viaLoc;
        return this;
    }
    public static class CellView {
        public String icon;
        public int count;
        public int[] gold;
        public CellView(String icon) {
            this.icon = icon;
        }
    }

    public static class PirateView {
        public enum Note {pirate,drunk,trapped};
        public Loc loc = new Loc(0,0);
        public Loc viaLoc = null;
        public boolean hidden = false;
        public boolean dead = false;
        public boolean rumReady = false;
        public int index = 0;
        public Loc[] steps;;
        public Loc[] stepsWithGold;;
        public List<Note> notes = new ArrayList<>();
    }

    public static class AnimateShip {
        public Loc from, to;
        public AnimateShip(Loc from, Loc to) {
            this.from = from;
            this.to = to;
        }
    }
    public static class AnimateRum {
        public int count;
        public Loc from, to;
        public AnimateRum(int count, Loc from, Loc to) {
            this.count = count;
            this.from = from;
            this.to = to;
        }
    }
}
