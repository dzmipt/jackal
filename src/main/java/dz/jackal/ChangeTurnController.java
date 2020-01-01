package dz.jackal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class ChangeTurnController extends GameController {

    private final static Logger log = LoggerFactory.getLogger(ChangeTurnController.class);

    @MessageMapping("/prevTurn")
    @SendTo("/jackal/view")
    public View prevTurn(ChangeRequest request) {
        game = Game.getGame(request.id);
        int turn = game.getTurn();
        if (! game.continueTurn()) turn--;

        if (turn < 0) {
            log.warn("No previous turn");
        } else {
            game = getGame(request.id, turn, game);
        }
        return getView(null);
    }

    @MessageMapping("/nextTurn")
    @SendTo("/jackal/view")
    public View nextTurn(ChangeRequest request) {
        game = Game.getGame(request.id);
        int turn = game.getTurn()+1;
        try {
            int lastTurn = DbGames.getLastTurn(request.id);
            if (turn > lastTurn) {
                log.warn("No next turn");
            } else {
                game = getGame(request.id, turn, game);
            }
        } catch (IOException e) {
            log.warn("Can't get last turn for id " + request.id, e);
        }
        return getView(null);
    }

    private Game getGame(String id, int turn, Game defaultGame) {
        Game game = defaultGame;
        try {
            game = DbGames.loadGame(id, turn);
            Game.putGame(game);
        } catch (IOException e) {
            log.error("Can't load game with id " + id + " turn " + turn);
        }
        return game;
    }

    public static class ChangeRequest {
        public String id;
    }
}
