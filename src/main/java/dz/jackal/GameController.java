package dz.jackal;

import dz.jackal.cell.Cave;
import dz.jackal.cell.Cell;
import dz.jackal.cell.MoveCell;
import dz.jackal.cell.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

abstract class GameController {
    private final static Logger log = LoggerFactory.getLogger(GoController.class);

    protected Game game;

    public View action(Request request) {
        game = Game.getGame(request.id);
        int beforeStep = game.getTurn();
        View view = processAction(request);
        boolean continueTurn = beforeStep == game.getTurn();
        if (continueTurn) {
            game.setContinueTurn();
        } else {
            try {
                DbGames.saveGame(game);
            } catch (IOException e) {
                log.error("Can't save game " + game.getId(), e);
            }
        }
        return view;
    }

    protected View processAction(Request request) {
        throw new IllegalStateException();
    }

    protected View getView(Hero selHero){
        Map<HeroId, Loc[]> steps = new HashMap<>();
        Map<HeroId, Loc[]> stepsWithGold = new HashMap<>();
        Set<HeroId> rumReady = new HashSet<>();

        int currentTeam = game.getCurrentTeam();
        for(HeroId id:HeroId.ALL) {
            Hero hero = game.getHero(id);
            if (selHero == null || selHero.equals(hero)) {
                if (hero.team() == currentTeam) {
                    steps.put(id, whereCanGo(hero, false).toArray(new Loc[0]));
                    stepsWithGold.put(id, whereCanGo(hero, true).toArray(new Loc[0]));
                }
            }

            if (selHero == null && rumReady(hero)) rumReady.add(id);
        }

        return new View(game, steps, stepsWithGold, rumReady);
    }

    protected boolean canGo(Hero hero, Cell newCell) {
        if (newCell.closed()) return true;

        if (newCell.crocodile()) return false;
        if (hero.trapped()) return false;

        List<Hero> heroes = newCell.heroes(0);
        if (hero.friday() && game.hasEnemy(hero.team(),heroes)) return false;

        if (hero.missioner() && game.hasEnemy(hero.team(), heroes)) return false;
        Missioner missioner = game.getMissioner();
        if (heroes.contains(missioner) && missioner.missioner()
                && game.enemy(hero, missioner.team())
                && missioner.team()!=-1) return false;
        if ((newCell.woman() || newCell.fort()) && game.hasEnemy(hero, newCell.heroes(0))) return false;

        return true;
    }

    protected boolean canGoWithGold(Hero hero, Cell newCell) {
        if (newCell.closed()) return false;
        if (!canGo(hero, newCell)) return false;

        if (game.hasEnemy(hero, newCell.heroes(0))) return false;
        if (newCell.ship() && game.enemy(hero, ((Ship)newCell).team()) ) return false;
        if (hero.missioner()) return false;
        if (newCell.woman() || newCell.fort()) return false;

        if (newCell.cave() && whereCanGoFromCave(hero,((Cave)newCell).getExit()).size()==0) return false;
        return true;
    }

    protected boolean canGo(Hero hero, Cell newCell, boolean withGold) {
        if (withGold) return canGoWithGold(hero, newCell);
        else return canGo(hero, newCell);
    }

    protected List<Loc> whereCanGo(Hero hero, boolean withGold) {
        List<Loc> steps = new ArrayList<>();
        if (hero.dead() || hero.inCave() || hero.team() == -1) return steps;
        Loc loc = hero.getLoc();
        Cell cell = game.getCell(loc);
        int index = cell.index(hero);
        if (cell.ship() && withGold) return steps;

        if (withGold && cell.gold(index) == 0) return steps;
        if (cell.move()) {
            return whereCanGoFromMove(hero, withGold);
        } else if (cell.cave() && hero.getInitStepLoc()!= null) {
            return whereCanGoFromCave(hero, ((Cave)cell).getExit());
        }

        if (cell.multiStep()) {
            boolean moveFromTheCell = hero.friday() || index + 1 == cell.count() || hero.drunk();

            if (!moveFromTheCell) {
                if (!withGold) {
                    steps.add(loc);
                } else {
                    if (!hero.missioner()) {
                        boolean hasEnemy = game.hasEnemy(hero, cell.heroes(index + 1));
                        if (!hasEnemy) steps.add(loc);
                    }
                }
                return steps;
            }
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
                    if (!canGo(hero,newCell, withGold)) continue;
                } else { // on land
                    if (newCell.sea()) continue;
                    if (!canGo(hero, newCell, withGold)) continue;
                }

                steps.add(newLoc);
            }
        }
        return  steps;
    }

    private List<Loc> whereCanGoFromMove(Hero hero, boolean withGold) {
        List<Loc> steps = new ArrayList<>();
        MoveCell cell = (MoveCell) game.getCell(hero.getLoc());
        Loc[] moveSteps = cell.nextSteps(hero.getPrevLoc(), hero.getLoc());
        for (Loc step:moveSteps) {
            if (step.row()<0 || step.row()>12 || step.col()<0 || step.col()>12) continue;
            if (!canGo(hero, game.getCell(step), withGold)) continue;
            steps.add(step);
        }
        return steps;
    }

    protected List<Loc> whereCanGoFromCave(Hero hero, int caveEntrance) {
        return game.caveLocs().stream()
                    .filter(loc -> caveEntrance != ((Cave)game.getCell(loc)).getExit() )
                    .filter(loc -> ! game.getCell(loc).closed())
                    .filter(loc -> {
                        Cell cell = game.getCell(loc);
                        List<Hero> heroes = cell.heroes(0);
                        if (heroes.size() == 0) return true;
                        return ! game.enemy(hero.team(), heroes.get(0).team());
                    }).collect(Collectors.toList());
    }

    private boolean rumReady(Hero hero) {
        if (hero.dead() || hero.inCave() || hero.team() == -1) return false;

        int currentTeam = game.getCurrentTeam();
        if (hero.friday() || hero.missioner() || hero.team() == currentTeam) {
            if (game.getAllTeamRum(currentTeam)>0 && canDrink(hero)) {
                return true;
            }
        }

        return false;
    }

    private boolean canDrink(Hero hero) {
        if (hero.dead() || hero.inCave()) return false;
        if ( (hero.friday() || hero.missioner()) && ! hero.inCave() ) {
            return HeroId.ALL.stream()
                    .filter(id -> id.team() == game.getCurrentTeam())
                    .anyMatch(id -> canGiveBottle(hero, game.getHero(id)));
        } else {
            if (hero.trapped()) return true;
            Cell cell = game.getCell(hero.getLoc());
            int index = cell.index(hero);
            if (index+1<cell.count()) return true;

            return false;
        }
    }

    private boolean canGiveBottle(Hero target, Hero from) {
        if (from.dead() || target.dead() || from.inCave() || target.inCave()) return false;
        Loc fromLoc = from.getLoc();
        Cell fromCell = game.getCell(fromLoc);
        int fromIndex = fromCell.index(from);
        Loc targetLoc = target.getLoc();
        Cell targetCell = game.getCell(targetLoc);
        int targetIndex = targetCell.index(target);
        if (fromLoc.equals(targetLoc)) {
            return targetIndex == fromIndex || targetIndex == fromIndex+1;
        }

        if (fromIndex < fromCell.count()-1) {
            return false;
        }

        return fromLoc.distance(targetLoc) == 1 && targetIndex == 0;
    }

    public static class Request {
        public String id;

        protected HeroId normalize(HeroId heroId) {
            if (heroId.equals(HeroId.BENGUNN_ID)) return HeroId.BENGUNN_ID;
            else if (heroId.equals(HeroId.FRIDAY_ID)) return HeroId.FRIDAY_ID;
            else if (heroId.equals(HeroId.MISSIONER_ID)) return HeroId.MISSIONER_ID;

            return heroId;
        }
    }
}
