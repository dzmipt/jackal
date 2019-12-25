package dz.jackal;

import dz.jackal.cell.Cell;
import dz.jackal.cell.MoveCell;
import dz.jackal.cell.ShipCell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class View {

    public String id;
    public CellView[][] cells;
    public List<PirateView> heroes;
    public AnimateShip animateShip = null;
    public AnimateRum animateRum = null;
    public Loc[] ship = new Loc[4];
    public int[] gold = new int[4];
    public int[] rum = new int[4];
    public int currentTeam;
    public int benGunnTeam, fridayTeam, missionerTeam;

    public View(Game game) {
        init(game, null);
    }

    public View(Game game, Hero selHero) {
        init(game, selHero);
    }

    private void init(Game game, Hero selHero) {
        id = game.getId();

        initCellView(game);
        initPirateView(game, selHero);
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

    private void initPirateView(Game game, Hero selHero) {
        heroes = new ArrayList<>();
        for(HeroId id: HeroId.ALL) {
            Hero hero = game.getHero(id);
            PirateView pirateView = new PirateView();
            heroes.add(pirateView);

            if (hero.dead()) continue;
            if (game.getCell(hero.getLoc()).closed()) {// additional heroes not discovered
                continue;
            }

            Loc loc = hero.getLoc();
            Cell cell = game.getCell(loc);
            int index = cell.index(hero);
            boolean hasGold = cell.gold(index) > 0;

            pirateView.loc = loc;
            pirateView.index = index;
            pirateView.hidden = false;

            if (selHero != null && ! hero.equals(selHero)) continue;
            if (game.getCurrentTeam() != hero.team() ) continue;

            if (cell.multiStep()) {
                if (hero.friday() || index + 1 == cell.count()) {
                    addSteps(pirateView, game, loc, hero, hasGold);
                } else {
                    pirateView.steps.add(loc);
                    if (hasGold && !hero.missioner()) {
                        boolean hasEnemy = game.hasEnemy(hero, cell.heroes(index + 1));
                        if (!hasEnemy) pirateView.stepsWithGold.add(loc);
                    }
                    if (!hero.missioner() && game.getAllTeamRum(hero.team())>0) {
                        pirateView.rumReady = true;
                    }
                }
            } else {
                addSteps(pirateView, game, loc, hero, hasGold);
            }
        }

    }

    private void addSteps(PirateView pirateView, Game game, Loc loc, Hero hero, boolean hasGold) {
        Cell cell = game.getCell(loc);

        if (cell.move()) {
            addStepsMove(pirateView,game, hero, hasGold);
        }

        for (int dr=-1; dr<=1; dr++) {
            for (int dc=-1; dc<=1; dc++) {
                if (dr == 0 && dc == 0) continue;

                int r = loc.row()+dr;
                int c = loc.col()+dc;
                if (r<0 || r>12 || c<0 || c>12) continue;
                Loc newLoc = new Loc(r,c);
                Cell newCell = game.getCell(newLoc);

                if (cell.sea()) {
                    if (newCell.land()) continue;
                } else if (cell.ship()) {
                    boolean diag = ! (dr == 0 || dc == 0);
                    if (diag) continue;
                    if (newCell.sea()) {
                        if (r == 1 || r ==11 || c==1 || c==11) continue;
                    }
                } else { // on land
                    if (newCell.sea()) continue;
                    if (newCell.ship() && game.enemy(hero, ((ShipCell)newCell).team()) ) continue;
                }

                List<Hero> heroes = newCell.heroes(0);
                if (hero.friday() && game.hasEnemy(hero.team(),heroes)) continue;

                pirateView.steps.add(newLoc);
                if (cell.ship()) continue;
                if (! hasGold) continue;
                if (! canGoWithGold(game, hero,newCell)) continue;

                pirateView.stepsWithGold.add(newLoc);
            }
        }
    }

    private void addStepsMove(PirateView pirateView, Game game, Hero hero, boolean hasGold){
        MoveCell cell = (MoveCell) game.getCell(hero.getLoc());
        Loc[] steps = cell.nextSteps(hero.getPrevLoc(), hero.getLoc());
        for (Loc step:steps) {
            if (step.row()<0 || step.row()>12 || step.col()<0 || step.col()>12) continue;
            pirateView.steps.add(step);
            if (!hasGold) continue;
            if (! canGoWithGold(game, hero, game.getCell(step))) continue;

            pirateView.stepsWithGold.add(step);
        }
    }

    private boolean canGoWithGold(Game game, Hero hero, Cell newCell) {
        if (newCell.closed()) return false;
        if (newCell.sea()) return false;
        if (game.hasEnemy(hero, newCell.heroes(0))) return false;
        if (newCell.ship() && game.enemy(hero, ((ShipCell)newCell).team()) ) return false;
        if (hero.missioner()) return false;

        return true;
    }

    public View setAnimateShip(AnimateShip animateShip) {
        this.animateShip = animateShip;
        return this;
    }

    public View setAnimateRum(AnimateRum animateRum) {
        this.animateRum = animateRum;
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
        public Loc loc = new Loc(0,0);
        public boolean hidden = true;
        public boolean rumReady = false;
        public int index = 0;
        public List<Loc> steps = new ArrayList<>();
        public List<Loc> stepsWithGold = new ArrayList<>();
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
