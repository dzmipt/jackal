package dz.jackal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

abstract class GameController {
    private final static Logger log = LoggerFactory.getLogger(GoController.class);

    public View action(Request request) {
        Game game = Game.getGame(request.id);
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
    }
}
