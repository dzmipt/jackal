package dz.jackal;

import dz.jackal.cell.Cell;
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
