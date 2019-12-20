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
    public int gold,rum;


    public View(Game game) {
        id = game.getId();

        initCellView(game);
        initPirateView(game);
        int team = game.getCurrentTeam();
        gold = game.getTeamGold(team);
        rum = game.getTeamRum(team);
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
        PirateId movePirateId = null;
        pirates = new PirateView[4][3];
        for(PirateId id: PirateId.ALL) {
            Pirate pirate = game.getPirate(id);
            PirateView pirateView;
            int index;
            if (pirate.dead()) {
                pirateView = new PirateView(new Loc(0,0));
                pirateView.dead = true;
                index = 0;
            } else {
                Loc loc = pirate.getLoc();
                Cell cell = game.getCell(loc);
                if (cell.move()) movePirateId = id;
                index = cell.index(pirate);
                boolean hasGold = cell.gold(index) > 0;

                if (cell.multiStep()) {
                    if (index + 1 == cell.count()) {
                        pirateView = getPirateView(game, loc, pirate, hasGold);
                    } else {
                        pirateView = new PirateView(loc);
                        pirateView.steps.add(loc);
                        if (hasGold) {
                            boolean hasEnemy = game.hasEnemy(pirate, cell.heroes(index + 1));
                            if (!hasEnemy) pirateView.stepsWithGold.add(loc);
                        }
                    }
                } else {
                    pirateView = getPirateView(game, loc, pirate, hasGold);
                }
            }

            pirateView.index = index;
            pirates[id.team()][id.num()] = pirateView;
        }

        if (movePirateId != null) {
            for (PirateId id: PirateId.ALL) {
                if (id.equals(movePirateId)) continue;
                pirates[id.team()][id.num()].steps.clear();
                pirates[id.team()][id.num()].stepsWithGold.clear();
            }
        }
    }

    private PirateView getPirateView(Game game, Loc loc, Pirate pirate, boolean hasGold) {
        Cell cell = game.getCell(loc);
        PirateView pirateView = new PirateView(loc);
        pirateView.dead = pirate.dead();
        if (pirateView.dead) return pirateView;
        if (game.getCurrentTeam() != pirate.team() ) return pirateView;

        if (cell.move()) return getPirateMoveView(game, pirateView, pirate, hasGold);

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
                    if (newCell.ship() && game.enemy(pirate.team(), ((ShipCell)newCell).team()) ) continue;
                }

                pirateView.steps.add(newLoc);
                if (cell.ship()) continue;
                if (! hasGold) continue;
                if (newCell.closed()) continue;
                if (game.hasEnemy(pirate, newCell.heroes(0))) continue;

                pirateView.stepsWithGold.add(newLoc);
            }
        }
        return pirateView;
    }

    private PirateView getPirateMoveView(Game game, PirateView pirateView, Pirate pirate, boolean hasGold){
        MoveCell cell = (MoveCell) game.getCell(pirate.getLoc());
        Loc[] steps = cell.nextSteps(pirate.getPrevLoc(), pirate.getLoc());
        for (Loc step:steps) {
            if (step.row()<0 || step.row()>12 || step.col()<0 || step.col()>12) continue;
            pirateView.steps.add(step);
            if (!hasGold) continue;
            Cell newCell = game.getCell(step);
            if (newCell.closed()) continue;
            if (newCell.sea()) continue;
            if (game.hasEnemy(pirate, newCell.heroes(0))) continue;
            if (newCell.ship() && game.enemy(pirate.team(), ((ShipCell)newCell).team()) ) continue;

            pirateView.stepsWithGold.add(step);
        }
        return pirateView;
    }

    public View setAnimateShip(AnimateShip animateShip) {
        this.animateShip = animateShip;
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
}
