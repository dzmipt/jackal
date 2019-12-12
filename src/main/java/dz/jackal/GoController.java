package dz.jackal;

import javafx.util.Pair;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.nio.channels.MulticastChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class GoController {
    @MessageMapping("/go")
    @SendTo("/jackal/view")
    public View action(GoRequest request) {
        Game game = Game.getGame(request.id);
        Pirate pirate = game.getPirate(request.pirate);
        Loc oldLoc = pirate.getLoc();
        Cell oldCell = game.getCell(oldLoc);
        Loc newLoc = request.loc;
        Cell newCell = game.getCell(newLoc);
        pirate.setPrevLoc(oldLoc);
        if (!oldCell.move()) pirate.setInitStepLoc(oldLoc);


        View.AnimateShip animateShip = null;
        if (oldCell.ship() && newCell.sea()) {
            game.moveShip(((ShipCell)oldCell).team(), newLoc);
            animateShip = new View.AnimateShip(oldLoc, newLoc);
        } else {
            newCell.open();
            game.movePirate(pirate, newLoc, request.withGold);

            if (newCell.move()) {
                if (isCycle(game, pirate, newLoc)) {
                    pirate.die();
                    int index = newCell.index(pirate);
                    newCell.removeHero(index, pirate);
                }
            }

            if (oldCell.move() && oldCell.gold(0)>0) {
                if (pirate.dead() || !request.withGold) {
                    oldCell.takeGold(0);
                    Cell initCell = game.getCell(pirate.getInitStepLoc());
                    initCell.addGold(initCell.count() - 1);
                }
            }


            if (pirate.dead()) {
            } else if (newCell.ship()) {
                if (game.enemy(pirate.team(), ((ShipCell)newCell).team())) game.returnToShip(pirate);
            } else {
                int index = newCell.index(pirate);
                newCell.heroes(index)
                        .stream()
                        .filter(p -> game.enemy(p, pirate))
                        .forEach(p -> game.returnToShip(p));
            }
        }

        return game.getView().setAnimateShip(animateShip);
    }

    private boolean isCycle(Game game, Pirate pirate, Loc loc) {
        List<PairLoc> newLoc = new ArrayList<>();
        newLoc.add(new PairLoc(pirate.getPrevLoc(), loc));
        Set<PairLoc> allLoc = new HashSet<>(newLoc);
        while (newLoc.size() > 0) {
            List<PairLoc> nextNewLoc = new ArrayList<>();
            for(PairLoc locs:newLoc) {
                Cell newCell = game.getCell(locs.newLoc);
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

    public static class GoRequest {
        public String id;
        public PirateId pirate;
        public Loc loc;
        public boolean withGold;
    }

}
