package dz.jackal;

import dz.jackal.cell.Cell;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class DrinkController extends GameController {

    @MessageMapping("/drink")
    @SendTo("/jackal/view")
    public View action(DrinkRequest request) {
        return super.action(request);
    }

    @Override
    protected View processAction(Request aRequest) {
        DrinkRequest request = (DrinkRequest) aRequest;

        Game game = Game.getGame(request.id);

        Hero hero = game.getHero(request.heroId);
        Cell cell = game.getCell(hero.getLoc());
        int index = cell.index(hero);
        cell.removeHero(index, hero);
        cell.addHero(cell.count()-1, hero);

        game.drinkRumBottle(hero.team());
        return game.getView(hero);
    }

    public static class DrinkRequest extends Request {
        public HeroId heroId;
    }
}
