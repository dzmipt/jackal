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
        Game game = Game.newGame();
        try {
            DbGames.saveGame(game);
        } catch (IOException e) {
            log.error("Can't save game " + game.getId(), e);
        }
        return game.getView();
    }
    public static class InitRequest{}
}
