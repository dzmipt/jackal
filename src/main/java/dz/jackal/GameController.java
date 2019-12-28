package dz.jackal;

import dz.jackal.cell.Cell;
import dz.jackal.cell.ShipCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

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

    abstract protected View processAction(Request request);

    public static boolean canGo(Game game, Hero hero, Cell newCell, boolean withGold) {
        if (newCell.closed() && !withGold) return true;

        List<Hero> heroes = newCell.heroes(0);
        if (hero.friday() && game.hasEnemy(hero.team(),heroes)) return false;

        if (hero.missioner() && game.hasEnemy(hero.team(), heroes)) return false;
        Missioner missioner = game.getMissioner();
        if (heroes.contains(missioner) && missioner.missioner()
                && game.enemy(hero, missioner.team())
                && missioner.team()!=-1) return false;

        if (newCell.woman() && game.hasEnemy(hero, newCell.heroes(0))) return false;

        if (!withGold) return true;

        if (newCell.closed()) return false;
        if (newCell.sea()) return false;
        if (game.hasEnemy(hero, newCell.heroes(0))) return false;
        if (newCell.ship() && game.enemy(hero, ((ShipCell)newCell).team()) ) return false;
        if (hero.missioner()) return false;
        if (newCell.woman()) return false;

        return true;
    }


    public interface TurnController {
        View process();
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
