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
import java.util.stream.Stream;

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
        if (hero.trapped()) return false;
        if (newCell.closed()) return true;

        if (newCell.crocodile()) return false;

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

        loc.allAround().forEach(newLoc -> {
            Cell newCell = game.getCell(newLoc);
            if (newCell == null) return; // no cell at the location;

            if (cell.sea()) {
                if (newCell.land()) return;
            } else if (cell.ship()) {
                if (newCell.sea()) return; // ship sailing is below
                if (newLoc.diagonal(loc)) return;
                if (!canGo(hero,newCell, withGold)) return;
            } else { // on land
                if (newCell.sea()) return;
                if (!canGo(hero, newCell, withGold)) return;
            }

            steps.add(newLoc);
        });

        steps.addAll(whereCanSail(loc));
        return steps;
    }

    private Set<Loc> whereCanSail(Loc loc ) {
        Set<Loc> sailTo = new HashSet<>();
        Cell cell = game.getCell(loc);
        if (! cell.ship()) return sailTo;
        long pirateCount = cell.heroes(0).stream()
                .filter(h -> !h.friday())
                .filter(h -> !h.missioner()).count();
        sailTo.add(loc);
        Set<Loc> border = new HashSet<>(sailTo);
        for(int i=0; i<pirateCount; i++) {
            border = border.stream()
                    .flatMap(Loc::around)
                    .filter(l -> ! sailTo.contains(l))
                    .filter(l -> game.getCell(l)!= null)
                    .filter(l -> !game.getCell(l).land())
                    .filter(l -> l.around().map(la -> game.getCell(la)).filter(Objects::nonNull).anyMatch(Cell::land))
                    .collect(Collectors.toSet());
            sailTo.addAll(border);
        }
        sailTo.remove(loc);
        return sailTo;
    }

    private List<Loc> whereCanGoFromMove(Hero hero, boolean withGold) {
        List<Loc> steps = new ArrayList<>();
        MoveCell cell = (MoveCell) game.getCell(hero.getLoc());
        Loc[] moveSteps = cell.nextSteps(hero.getPrevLoc(), hero.getLoc());
        for (Loc step:moveSteps) {
            Cell newCell = game.getCell(step);
            if (newCell == null) continue;
            if (!canGo(hero, newCell, withGold)) continue;
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

    protected void die(Hero hero) {
        Cell cell = game.getCell(hero.getLoc());
        if (cell != null) {
            int index = cell.index(hero);
            cell.removeHero(index, hero);
        }
        hero.die();
    }

    private void checkCaves() {
        List<Hero> heroes = HeroId.ALL.stream()
                .map(id -> game.getHero(id))
                .filter(hero -> hero.inCave())
                .collect(Collectors.toList());
        while (heroes.size()>0) {
            Hero hero = heroes.remove(Game.random.nextInt(heroes.size()));
            List<Loc> steps = whereCanGoFromCave(hero, hero.getCaveExit());
            if (steps.size() > 0) {
                Loc loc = steps.get(Game.random.nextInt(steps.size()));
                game.getCell(loc).addHero(0, hero);
                hero.setLoc(loc);
                hero.exitFromCave();
            }
        }
    }

    private void checkWoman() {
        Hero[] heroes = HeroId.ALL.stream()
                .filter(id -> id.team() == game.getCurrentTeam()) // don't include additional heroes
                .map(id -> game.getHero(id))
                .toArray(Hero[]::new);

        Hero body =  Stream.of(heroes).filter(Hero::dead).findFirst().orElse(null);
        if (body == null) return;

        Hero father = game.woman().heroes(0)
                .stream()
                .filter(h -> h.id().team() == game.getCurrentTeam())
                .findFirst().orElse(null);

        if (father == null) return;
        body.birth(father.getLoc());
        game.getCell(father.getLoc()).addHero(0, body);
    }

    private void moveBear() {
        if (game.getCurrentTeam() != game.getBearTeamTurn()) return;


        List<Hero> heroes = HeroId.ALL.stream()
                .map(id -> game.getHero(id))
                .filter(h -> !h.dead())
                .filter(h -> !h.inCave())
                .filter(h -> h.team() != -1)
                .filter(h -> game.getCell(h.getLoc()).land())
                .collect(Collectors.toList());
        if (heroes.size() == 0) return;

        Loc loc = game.getBearLoc();
        int dist = heroes.stream()
                .mapToInt(h -> h.getLoc().distance(loc))
                .min().orElse(-1);
        heroes = heroes.stream()
                .filter(h -> h.getLoc().distance(loc) == dist)
                .collect(Collectors.toList());
        Hero hero = heroes.get(Game.random.nextInt(heroes.size()));
        Loc newLoc = loc.stepTo(hero.getLoc());
        game.setBearLoc(newLoc);
        Cell cell = game.getCell(newLoc);
        int count = cell.count();
        for (int index=0; index<count; index++) {
            cell.heroes(index).forEach(h -> game.returnToShip(h));
        }
    }

    protected void nextTurn() {
        checkCaves();
        game.nextTurn();
        for(HeroId id: HeroId.ALL) {
            Hero hero = game.getHero(id);
            hero.setDrunk(false);
            hero.setInitStepLoc(null);
        }
        for(Loc loc:Loc.ALL) {
            game.getCell(loc).nextStep();
        }
        checkWoman();
        moveBear();
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
