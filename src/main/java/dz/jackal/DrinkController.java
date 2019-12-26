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

        Hero selHero = null;
        Hero hero = game.getHero(request.getHeroId());
        if(hero.friday()) {
            hero.die();
        } else if (hero.missioner()) {
            ((Missioner)hero).drinkToPirate();
        } else {
            hero.setDrunk(true);
            selHero = hero;
        }

        game.drinkRumBottle(game.getCurrentTeam());
        return game.getView(selHero);
    }

    public static class DrinkRequest extends Request {
        private HeroId heroId;

        public void setHeroId(HeroId id) {
            this.heroId = normalize(id);
        }

        public HeroId getHeroId() {
            return heroId;
        }
    }
}
