package dz.jackal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
public class ListGamesController {
    private final static Logger log = LoggerFactory.getLogger(ListGamesController.class);

    private final Format formatter = new SimpleDateFormat("E, dd MMM yy hh:mm");

    @MessageMapping("/list")
    @SendTo("/jackal/list")
    public ListGames list() {
        try {
            String[] ids = DbGames.getIds();
            ListGames list = new ListGames();
            for (String id: ids) {
                try {
                    GameView game = new GameView(id);
                    game.last = DbGames.getLastTurn(id);
                    long from = DbGames.getTime(id, 0);
                    long to = DbGames.getTime(id, game.last);
                    game.from = formatter.format(new Date(from));
                    game.to = formatter.format(to);
                    game.millis = to;

                    list.games.add(game);
                } catch (IOException e) {
                    log.error("Error on getting info on game id " + id, e);
                }
            }

            Collections.sort(list.games);
            return list;
        } catch (IOException e) {
            log.error("Can't load game list", e);
            throw new IllegalStateException("Can't load game list", e);
        }
    }

    @MessageMapping("/new")
    @SendTo("/jackal/new")
    public NewGameResponse newGame(String[] names) {
        Game game = Game.newGame(names);
        try {
            DbGames.saveGame(game);
        } catch (IOException e) {
            log.error("Can't save game " + game.getId(), e);
        }
        return new NewGameResponse( game.getId() );
    }

    public static class ListGames {
        public List<GameView> games = new ArrayList<>();
    }

    public static class GameView implements Comparable {
        public String id;
        private long millis;
        public int last;
        public String from, to;

        @Override
        public int compareTo(Object o) {
            GameView game = (GameView) o;
            if (game.millis == this.millis) return 0;
            if (game.millis > this.millis) return 1;
            else return -1;
        }

        public GameView(String id) {
            this.id = id;
        }
    }

    public static class NewGameResponse {
        public String id;
        public NewGameResponse(String id) {
            this.id = id;
        }
    }
}
