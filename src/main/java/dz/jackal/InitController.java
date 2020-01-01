package dz.jackal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.IOException;


@Controller
public class InitController {
    private final static Logger log = LoggerFactory.getLogger(InitController.class);
    @MessageMapping("/init")
    @SendTo("/jackal/view")
    public View action(InitRequest request) {
        Game game;
        if (request.id == null) {
            game = newGame();
        } else {
            game = loadGame(request.id);
        }
        return game.getView();
    }

    private Game newGame() {
        Game game = Game.newGame(new String[]{"","","",""}, new int[] {0,1,0,1});
        try {
            DbGames.saveGame(game);
        } catch (IOException e) {
            log.error("Can't save game " + game.getId(), e);
        }
        return game;
    }

    private Game loadGame(String id) {
        try {
            int lastTurn = DbGames.getLastTurn(id);
            Game game = DbGames.loadGame(id, lastTurn);
            Game.putGame(game);
            return game;
        } catch (IOException e) {
            log.error("Failed to load game with id " + id, e);
            throw new IllegalArgumentException("Can't load game with id " + id, e);
        }
    }

    public static class InitRequest{
        public String id;
    }
}
