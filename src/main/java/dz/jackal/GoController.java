package dz.jackal;

import dz.jackal.cell.Cannon;
import dz.jackal.cell.Cell;
import dz.jackal.cell.MoveCell;
import dz.jackal.cell.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Stream;

@Controller
public class GoController extends GameController {
    private final static Logger log = LoggerFactory.getLogger(GoController.class);

    private Hero hero;
    private Loc oldLoc, newLoc;
    private Cell oldCell, newCell;
    private View.AnimateRum animateRum;
    private boolean withGold;

    @MessageMapping("/go")
    @SendTo("/jackal/view")
    public View action(GoRequest request) {
        return super.action(request);
    }

    @Override
    protected View processAction(Request aRequest) {
        GoRequest request = (GoRequest) aRequest;
        animateRum = null;
        Loc viaLoc = null;
        withGold = request.withGold;
        hero = game.getHero(request.getHeroId());
        oldLoc = hero.getLoc();
        oldCell = game.getCell(oldLoc);
        newLoc = request.loc;
        newCell = game.getCell(newLoc);
        hero.setPrevLoc(oldLoc);
        newCell.open();

        if (!oldCell.move()) hero.setInitStepLoc(oldLoc);

        if (!newCell.ship()) {
            int rum = newCell.countRum();
            if (rum > 0) {
                newCell.takeRum();
                if (hero.friday()) {
                    rum--;
                    hero.die();
                }
                if (hero.missioner()) {
                    rum--;
                    ((Missioner)hero).drinkToPirate();
                }
                game.addRum(hero.team(), rum);
                animateRum = new View.AnimateRum(rum, newLoc, game.getTeamShipLoc(hero.team()));
            }
        }

        if (! canGo(hero, newCell, withGold)) {
            hero.die();
        }

        if (newCell.cannibal() && !hero.friday()) {
            hero.die();
        }

        if (oldCell.ship() && newCell.sea()) { // sail the ship
            sailShip();
        } else if (!hero.dead()) { // Friday can be dead here
            if (hero.missioner()) {
                List<Hero> heroes = oldCell.heroes(oldCell.index(hero));
                heroes.remove(hero);
                if (heroes.size()>0 && game.hasEnemy(heroes.get(0), heroes)) {
                    heroes.forEach(game::returnToShip);
                }
            }
            if (newCell.balloon()) {
                newCell.open();
                viaLoc = newLoc;
                newCell = game.getTeamShip(hero.team());
                newLoc = game.getTeamShipLoc(hero.team());
            }
            if (newCell.cannon()) {
                newCell.open();
                viaLoc = newLoc;
                newLoc = ((Cannon)newCell).fire(newLoc);
                newCell = game.getCell(newLoc);
            }

            if (newCell.trap()) {
                List<Hero> heroes = newCell.heroes(0);
                if (heroes.size() == 0) {
                    if (! hero.friday()) {
                        hero.setTrapped(true);
                    }
                } else {
                    heroes.get(0).setTrapped(false);
                }
            }
            moveHero();
        }

        if (oldCell.move() && oldCell.gold(0)>0) {
            if (hero.dead() || !withGold) { // for the move cell, return gold to the init loc
                oldCell.takeGold(0);
                Cell initCell = game.getCell(hero.getInitStepLoc());
                initCell.addGold(initCell.count() - 1);
            }
        }

        if (hero.dead() || ! newCell.move()) {
            nextTurn();
        }

        checkWoman();

        return getView(newCell.move() && !hero.dead() ? hero: null)
                    .setAnimateRum(animateRum)
                    .setViaLoc(hero.id(), viaLoc);
    }

    private void sailShip() {
        int theTeam = ((Ship)oldCell).team();
        game.getCell(newLoc).heroes(0).forEach(
                h-> {
                    if (h.friday()) {
                        h.setTeam(theTeam);
                    }
                    if(game.enemy(h,theTeam)) {
                        game.returnToShip(h);
                    } else {
                        game.moveHero(h, oldLoc, false);
                    }
                }
        );

        game.moveShip(((Ship)oldCell).team(), newLoc);
    }

    private void moveHero() {
        game.moveHero(hero, newLoc, withGold);

        if (newCell.move()) {
            if (isCycle()) {
                hero.die();
                int index = newCell.index(hero);
                newCell.removeHero(index, hero);
            }
        }

        if (hero.dead()) {
        } else if (newCell.ship()) {
            if (game.enemy(hero, ((Ship)newCell).team())) {
                // should be more rules for non pirates
                game.returnToShip(hero);
            }
        } else {
            int index = newCell.index(hero);
            List<Hero> heroes = newCell.heroes(index);


            heroes.stream() // discovery of additional hero
                    .filter(h -> h.team() == -1)
                    .forEach(h -> {
                        h.setTeam(hero.team());
                        if (h.id().equals(HeroId.BENGUNN_ID)) newCell.setTempIcon(Icon.BENGUNN);
                        else if (h.id().equals(HeroId.FRIDAY_ID)) newCell.setTempIcon(Icon.FRIDAY);
                        else if (h.id().equals(HeroId.MISSIONER_ID)) newCell.setTempIcon(Icon.MISSIONER);
                        else throw new IllegalStateException("Unknown hero " + h);
                    });

            Hero friday = game.getHero(HeroId.FRIDAY_ID);
            if (heroes.contains(friday)) { // Friday joins new team
                friday.setTeam(hero.team());
            }


            Hero missioner = game.getMissioner();
            if (! ( missioner.missioner() && heroes.contains(missioner) )) { // no fight on a cell with Missioner
                newCell.heroes(index) // fight
                        .stream()
                        .filter(h -> game.enemy(h, hero))
                        .forEach(h -> game.returnToShip(h));
            }

            if (missioner.missioner() && heroes.contains(missioner) && heroes.contains(friday)) {
                friday.die();
                missioner.die();
            }
        }
    }

    private boolean isCycle() {
        List<PairLoc> newLocs = new ArrayList<>();
        newLocs.add(new PairLoc(hero.getPrevLoc(), newLoc));
        Set<PairLoc> allLoc = new HashSet<>(newLocs);
        while (newLocs.size() > 0) {
            List<PairLoc> nextNewLoc = new ArrayList<>();
            for(PairLoc locs:newLocs) {
                Cell newCell = game.getCell(locs.newLoc);
                if (newCell == null) continue; // Knight jumps outside field
                if (newCell.closed()) return false;
                if (! canGo(hero, newCell)) continue;
                if (!newCell.move()) {
                    return false;
                }
                Loc[] steps = ((MoveCell)newCell).nextSteps(locs.prevLoc, locs.newLoc);
                for(Loc step: steps) {
                    PairLoc pl = new PairLoc(locs.newLoc, step);
                    if (allLoc.contains(pl)) continue;
                    allLoc.add(pl);
                    nextNewLoc.add(pl);
                }
            }
            newLocs = nextNewLoc;
        }

        return true;
    }

    public void nextTurn() {
        game.nextTurn();
        HeroId.ALL.forEach(id -> game.getHero(id).setDrunk(false));
        for(Loc loc:Loc.ALL) {
            game.getCell(loc).nextStep();
        }
    }

    private void checkWoman() {
        Hero[] heroes = HeroId.ALL.stream()
                .filter(id -> id.team() == game.getCurrentTeam()) // don't include additional heroes
                .map(id -> game.getHero(id))
                .toArray(Hero[]::new);

        Hero body =  Stream.of(heroes).filter(Hero::dead).findFirst().orElse(null);
        if (body == null) return;

        Hero father = game.getWoman().heroes(0)
                .stream()
                .filter(h -> h.id().team() == game.getCurrentTeam())
                .findFirst().orElse(null);

        if (father == null) return;
        body.birth(father.getLoc());
        game.getCell(father.getLoc()).addHero(0, body);
    }

    private static class PairLoc {
        Loc prevLoc,newLoc;
        PairLoc(Loc prevLoc,Loc newLoc) {
            this.prevLoc = prevLoc;
            this.newLoc = newLoc;
        }

        @Override
        public int hashCode() {
            return prevLoc.hashCode()*1000+newLoc.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            PairLoc pairLoc = (PairLoc) obj;
            return pairLoc.prevLoc.equals(prevLoc) && pairLoc.newLoc.equals(newLoc);
        }
    }

    public static class GoRequest extends Request {
        private HeroId heroId;
        public Loc loc;
        public boolean withGold;

        public void setHeroId(HeroId id) {
            heroId = normalize(id);
        }

        public HeroId getHeroId() {
            return heroId;
        }
    }

}
