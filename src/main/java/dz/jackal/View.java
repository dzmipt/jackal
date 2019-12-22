package dz.jackal;

import dz.jackal.cell.Cell;
import dz.jackal.cell.MoveCell;
import dz.jackal.cell.ShipCell;

import java.util.ArrayList;
import java.util.List;

public class View {

    public String id;
    public CellView[][] cells;
    public PirateView[][] pirates;
    public AnimateShip animateShip = null;
    public AnimateRum animateRum = null;
    public int[] gold = new int[4];
    public int[] rum = new int[4];
    public int currentTeam;

    public View(Game game) {
        id = game.getId();

        initCellView(game);
        initPirateView(game);
        currentTeam = game.getCurrentTeam();
        for(int team=0;team<4;team++) {
            gold[team] = game.getTeamGold(team);
            rum[team] = game.getTeamRum(team);
        }
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

    private void initPirateView(Game game) {
        HeroId moveHeroId = null;
        pirates = new PirateView[7][3];
        for(HeroId id: HeroId.ALL) {
            Hero hero = game.getHero(id);
            PirateView pirateView;
            int index;
            if (hero.dead()) {
                pirateView = new PirateView(new Loc(0, 0));
                pirateView.dead = true;
                index = 0;
            } else if (game.getCell(hero.getLoc()).closed()) {// additional heroes not discovered
                pirateView = new PirateView(new Loc(0,0));
                pirateView.dead = true;
                index = 0;
            } else {
                Loc loc = hero.getLoc();
                Cell cell = game.getCell(loc);
                if (cell.move()) moveHeroId = id;
                index = cell.index(hero);
                boolean hasGold = cell.gold(index) > 0;

                if (cell.multiStep()) {
                    if (index + 1 == cell.count()) {
                        pirateView = getPirateView(game, loc, hero, hasGold);
                    } else {
                        pirateView = new PirateView(loc);
                        pirateView.steps.add(loc);
                        if (hasGold) {
                            boolean hasEnemy = game.hasEnemy(hero, cell.heroes(index + 1));
                            if (!hasEnemy) pirateView.stepsWithGold.add(loc);
                        }
                    }
                } else {
                    pirateView = getPirateView(game, loc, hero, hasGold);
                }
            }

            pirateView.index = index;
            pirates[id.group()][id.num()] = pirateView;
        }

        if (moveHeroId != null) {
            for (HeroId id: HeroId.ALL) {
                if (id.equals(moveHeroId)) continue;
                pirates[id.group()][id.num()].steps.clear();
                pirates[id.group()][id.num()].stepsWithGold.clear();
            }
        }
    }

    private PirateView getPirateView(Game game, Loc loc, Hero hero, boolean hasGold) {
        Cell cell = game.getCell(loc);
        PirateView pirateView = new PirateView(loc);
        pirateView.dead = hero.dead();
        if (pirateView.dead) return pirateView;
        if (game.getCurrentTeam() != hero.team() ) return pirateView;

        if (cell.move()) return getPirateMoveView(game, pirateView, hero, hasGold);

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
                    if (newCell.ship() && game.enemy(hero.team(), ((ShipCell)newCell).team()) ) continue;
                }

                pirateView.steps.add(newLoc);
                if (cell.ship()) continue;
                if (! hasGold) continue;
                if (newCell.closed()) continue;
                if (game.hasEnemy(hero, newCell.heroes(0))) continue;

                pirateView.stepsWithGold.add(newLoc);
            }
        }
        return pirateView;
    }

    private PirateView getPirateMoveView(Game game, PirateView pirateView, Hero hero, boolean hasGold){
        MoveCell cell = (MoveCell) game.getCell(hero.getLoc());
        Loc[] steps = cell.nextSteps(hero.getPrevLoc(), hero.getLoc());
        for (Loc step:steps) {
            if (step.row()<0 || step.row()>12 || step.col()<0 || step.col()>12) continue;
            pirateView.steps.add(step);
            if (!hasGold) continue;
            Cell newCell = game.getCell(step);
            if (newCell.closed()) continue;
            if (newCell.sea()) continue;
            if (game.hasEnemy(hero, newCell.heroes(0))) continue;
            if (newCell.ship() && game.enemy(hero.team(), ((ShipCell)newCell).team()) ) continue;

            pirateView.stepsWithGold.add(step);
        }
        return pirateView;
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
        public Loc loc;
        public boolean dead;
        public int index;
        public List<Loc> steps = new ArrayList<>();
        public List<Loc> stepsWithGold = new ArrayList<>();
        public PirateView(Loc loc) {
            this.loc = loc;
        }
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
