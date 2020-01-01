package dz.jackal;

import dz.jackal.cell.Cell;
import dz.jackal.cell.ShipCell;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

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

        Hero selHero = null;
        Hero hero = game.getHero(request.getHeroId());
        if(hero.friday()) {
            hero.die();
        } else if (hero.missioner()) {
            ((Missioner)hero).drinkToPirate();
            Cell cell = game.getCell(hero.getLoc());
            List<Hero> heroes = cell.heroes(cell.index(hero));
            if (game.hasEnemy(hero, heroes)) {
                heroes.forEach(game::returnToShip);
            }
        } else {
            if (hero.trapped()) {
                hero.setTrapped(false);
            } else {
                hero.setDrunk(true);
            }
            selHero = hero;
        }

        drinkRumBottle(game.getCurrentTeam());
        return getView(selHero);
    }

    private void drinkRumBottle(int theTeam) {
        int team = -1;
        if (game.getTeamRum(theTeam)>0) team = theTeam;
        else {
            for (int t=0;t<4;t++) {
                if (game.enemy(t, theTeam)) continue;
                if (game.getTeamRum(t)==0) continue;
                team = t;
                break;
            }
            if (team == -1) throw new IllegalStateException("No rum to drink in team " + theTeam);
        }
        ShipCell ship = game.getTeamShip(team);
        int rum = ship.countRum();
        ship.setRum(rum-1);
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
