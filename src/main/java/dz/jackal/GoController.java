package dz.jackal;

import dz.jackal.cell.Cell;
import dz.jackal.cell.MoveCell;
import dz.jackal.cell.ShipCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class GoController extends GameController {
    private final static Logger log = LoggerFactory.getLogger(GoController.class);

    @MessageMapping("/go")
    @SendTo("/jackal/view")
    public View action(GoRequest request) {
        return super.action(request);
    }

    @Override
    protected View processAction(Request aRequest) {
        GoRequest request = (GoRequest) aRequest;
        Game game = Game.getGame(request.id);
        Hero hero = game.getHero(request.getHeroId());
        final Loc oldLoc = hero.getLoc();
        Cell oldCell = game.getCell(oldLoc);
        Loc newLoc = request.loc;
        Cell newCell = game.getCell(newLoc);
        hero.setPrevLoc(oldLoc);
        if (!oldCell.move()) hero.setInitStepLoc(oldLoc);

        View.AnimateShip animateShip = null;
        View.AnimateRum animateRum = null;
        if (!newCell.ship()) {
            int rum = newCell.countRum();
            if (rum > 0) {
                game.addRum(hero.team(), rum);
                newCell.takeRum();
                animateRum = new View.AnimateRum(rum, newLoc, game.getTeamShipLoc(hero.team()));
            }
        }

        if (oldCell.ship() && newCell.sea()) { // sail the ship
            int theTeam = ((ShipCell)oldCell).team();
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

            game.moveShip(((ShipCell)oldCell).team(), newLoc);
            animateShip = new View.AnimateShip(oldLoc, newLoc);
            game.nextTurn();
        } else {
            if (hero.missioner()) {
                List<Hero> heroes = oldCell.heroes(oldCell.index(hero));
                heroes.remove(hero);
                if (heroes.size()>0 && game.hasEnemy(heroes.get(0), heroes)) {
                    heroes.forEach(game::returnToShip);
                }
            }

            newCell.open();
            game.moveHero(hero, newLoc, request.withGold);

            if (newCell.move()) {
                if (isCycle(game, hero, newLoc)) {
                    hero.die();
                    int index = newCell.index(hero);
                    newCell.removeHero(index, hero);
                }
            }

            if (oldCell.move() && oldCell.gold(0)>0) {
                if (hero.dead() || !request.withGold) { // for the move cell, return gold to the init loc
                    oldCell.takeGold(0);
                    Cell initCell = game.getCell(hero.getInitStepLoc());
                    initCell.addGold(initCell.count() - 1);
                }
            }

            if (hero.dead()) {
            } else if (newCell.ship()) {
                if (game.enemy(hero, ((ShipCell)newCell).team())) {
                    // should be more rules for non pirates
                    game.returnToShip(hero);
                }
            } else {
                int index = newCell.index(hero);
                List<Hero> heroes = newCell.heroes(index);


                heroes.stream() // discovery of additional hero
                        .filter(h -> h.team() == -1)
                        .forEach(h -> h.setTeam(hero.team()));

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
            if (hero.dead() || ! newCell.move()) {
                game.nextTurn();
            }
        }

        return game.getView()
                    .setAnimateShip(animateShip)
                    .setAnimateRum(animateRum);
    }


    private boolean isCycle(Game game, Hero hero, Loc loc) {
        List<PairLoc> newLoc = new ArrayList<>();
        newLoc.add(new PairLoc(hero.getPrevLoc(), loc));
        Set<PairLoc> allLoc = new HashSet<>(newLoc);
        while (newLoc.size() > 0) {
            List<PairLoc> nextNewLoc = new ArrayList<>();
            for(PairLoc locs:newLoc) {
                Cell newCell = game.getCell(locs.newLoc);
                if (newCell == null) continue; // Knight jumps outside field
                if (newCell.closed()) return false;
                if (!newCell.move()) return false;
                Loc[] steps = ((MoveCell)newCell).nextSteps(locs.prevLoc, locs.newLoc);
                for(Loc step: steps) {
                    PairLoc pl = new PairLoc(locs.newLoc, step);
                    if (allLoc.contains(pl)) continue;
                    allLoc.add(pl);
                    nextNewLoc.add(pl);
                }
            }
            newLoc = nextNewLoc;
        }

        return true;
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
