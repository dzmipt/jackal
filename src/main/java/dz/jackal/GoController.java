package dz.jackal;

import dz.jackal.cell.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Stream;

@Controller
public class GoController extends GameController {
    private Hero hero;
    private Hero selHero;
    private Loc oldLoc, newLoc, viaLoc;
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
        viaLoc = null;
        View.Adv adv = null;
        withGold = request.withGold;
        hero = game.getHero(request.getHeroId());
        oldLoc = hero.getLoc();
        oldCell = game.getCell(oldLoc);
        newLoc = request.loc;
        newCell = game.getCell(newLoc);
        if (hero.getInitStepLoc() == null) {
            hero.setInitStepLoc(oldLoc);
        }
        hero.setPrevLoc(oldLoc);
        selHero = null;

        if (newCell.closed()) {
            newCell.open();
            int gold = newCell.gold(0);
            if (gold>0) {
                newCell.setTempIconLocation(Icon.GOLD.getLocation() + gold);
            }
        }

        if (!newCell.ship()) {
            int rum = newCell.countRum();
            if (rum > 0) {
                newCell.takeRum();
                newCell.setTempIconLocation(Icon.RUM.getLocation() + rum);
                if (hero.friday()) {
                    rum--;
                    die(hero);
                }
                if (hero.missioner()) {
                    rum--;
                    ((Missioner)hero).drinkToPirate();
                }
                game.addRum(hero.team(), rum);
                animateRum = new View.AnimateRum(rum, newLoc, game.getTeamShipLoc(hero.team()));
            }
        }

        if (! canGo(hero, newCell, withGold) && !newCell.crocodile()) {
            die(hero);
        }

        if (newCell.cannibal() && !hero.friday()) {
            die(hero);
        }

        if (newCell.den()) {
            ((Den)newCell).activate();
            game.setBearLoc(newLoc);
            game.setBearTeamTurn(game.getCurrentTeam());
            returnToShip(false);
        }

        if (newLoc.equals(game.getBearLoc())) {
            returnToShip(false);
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
                returnToShip(withGold);
            }
            if (newCell.cannon()) {
                viaLoc = newLoc;
                Cannon cannon = (Cannon)newCell;
                newLoc = Stream.iterate(newLoc, cannon::fire)
                                .filter(l -> !game.getCell(l).land())
                                .findFirst().orElse(null);

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

            if (newCell.crocodile()) {
                if (whereCanGo(hero,false).size() == 0) {
                    die(hero);
                } else {
                    viaLoc = newLoc;
                    selHero = hero;
                }
            }

            moveHero();

            if (newCell.earthquake()) {
                ((Earthquake)newCell).activate();
                game.earthquake();
                adv = View.Adv.Earthquake;
            }

            if (newCell.teeHee()) {
                ((TeeHee)newCell).activate();
                game.setTeeHee();
            }

            if (newCell.cave() && !oldCell.cave()) {
                boolean canGo = whereCanGoFromCave(hero, ((Cave)newCell).getExit()).size()>0;
                if (canGo) {
                    selHero = hero;
                } else {
                    hero.jumpIntoCave(((Cave)newCell).getExit());
                    newCell.removeHero(0, hero);
                }
            }
        }

        if (oldCell.move() && oldCell.gold(0)>0) {
            if (hero.dead() || !withGold) { // for the move cell, return gold to the init loc
                oldCell.takeGold(0);
                Cell initCell = game.getCell(hero.getInitStepLoc());
                initCell.addGold(initCell.count() - 1);
            }
        }

        if (selHero == null) {
            nextTurn();
        }


        return getView(selHero)
                    .setAnimateRum(animateRum)
                    .setViaLoc(hero.id(), viaLoc)
                    .setAdv(adv);
    }

    private void sailShip() {
        int theTeam = ((Ship)oldCell).team();
        oldLoc.path(newLoc)
                .flatMap(l -> game.getCell(l).heroes(0).stream())
                .forEach(
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

    private void returnToShip(boolean withGold) {
        viaLoc = newLoc;
        newCell = game.getTeamShip(hero.team());
        newLoc = game.getTeamShipLoc(hero.team());
        this.withGold = withGold;
    }

    private void moveHero() {
        if (newCell.crocodile()) return;

        game.moveHero(hero, newLoc, withGold);

        if (newCell.move()) {
            if (isCycle()) {
                die(hero);
            } else {
                selHero = hero;
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
                die(friday);
                die(missioner);
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
