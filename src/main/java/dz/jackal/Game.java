package dz.jackal;

import dz.jackal.cell.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {
    private final static long serialVersionUID = 4;
    private final static Logger log = LoggerFactory.getLogger(Game.class);

    private String id;
    private Map<Loc, Cell> cells = new HashMap<>();
    private Cell woman;
    private Loc[] ships = new Loc[4];
    private Map<HeroId, Hero> heroes = new HashMap<>();
    private int currentTeam = 0;
    private int turn = 0;
    private boolean startTurn = true;

    private Game(String id) {
        this.id = id;
        cells = GameInitializer.init();
        //cells = GameInitializer.initTest();

        for(Map.Entry<Loc,Cell> entry: cells.entrySet()) {
            Loc loc = entry.getKey();
            Cell cell = entry.getValue();
            int count = cell.count();
            for (int index=0; index<count; index++) {
                for(Hero hero:cell.heroes(index)) {
                    heroes.put(hero.id(), hero);
                }
            }
            if (cell.ship()) {
                ships[((ShipCell)cell).team()] = loc;
            }
            if (cell.woman()) {
                woman = cell;
            }
        }
    }

    public Cell getWoman() {
        return woman;
    }

    public Cell getCell(Loc loc) {return cells.get(loc);}
    public Hero getHero(HeroId heroId) {return heroes.get(heroId);}
    public Missioner getMissioner() {return (Missioner) getHero(HeroId.MISSIONER_ID);}


    public Loc getTeamShipLoc(int team) {
        return ships[team];
    }
    public ShipCell getTeamShip(int team) {
        return (ShipCell) getCell(ships[team]);
    }
    public int getTeamGold(int team) {
        return getTeamShip(team).gold(0);
    }

    public int getTeamRum(int team) {
        return getTeamShip(team).countRum();
    }

    public int getAllTeamRum(int theTeam) {
        int rum = 0;
        for(int team=0;team<4;team++) {
            if (enemy(team, theTeam)) continue;
            rum += getTeamRum(team);
        }
        return rum;
    }

    public void addRum(int team, int rum) {
        Cell ship = getTeamShip(team);
        ship.setRum(ship.countRum() + rum);
    }

    public boolean enemy(int team1, int team2) {
        return (team1+team2) % 2 == 1;
        //return team1!=team2;
    }

    public boolean enemy(Hero h1, Hero h2) {
        if (h1.friday() || h2.friday()) return false;
        if (h1.missioner() || h2.missioner()) return false;
        return enemy(h1.team(), h2.team());
    }

    public boolean enemy(Hero h, int team) {
        if (h.friday() || h.missioner()) return false;
        return enemy(h.team(), team);
    }

    public boolean hasEnemy(Hero hero, Collection<Hero> heroes) {
        return heroes.stream()
                .anyMatch(h -> enemy(hero,h));
    }

    public boolean hasEnemy(int team, Collection<Hero> heroes) {
        return heroes.stream()
                .anyMatch(h -> enemy(h,team));
    }


    public void nextTurn() {
        currentTeam = (currentTeam+1) % 4;
        turn++;
        startTurn = true;
    }
    public void setContinueTurn() {
        startTurn = false;
    }
    public boolean continueTurn() {
        return !startTurn;
    }
    public int getTurn() {return turn;}

    public int getCurrentTeam() {
        return currentTeam;
    }

    public void moveShip(int team, Loc newLoc) {
        Loc oldLoc = ships[team];
        if (oldLoc.equals(newLoc)) return;
        ships[team] = newLoc;
        Cell newCell = cells.get(newLoc);
        Cell oldCell = cells.get(oldLoc);
        cells.put(newLoc,oldCell);
        cells.put(oldLoc,newCell);

        oldCell.heroes(0).forEach(h->h.setLoc(newLoc));
    }

    public void moveHero(Hero h, Loc newLoc, boolean withGold) {
        Loc oldLoc = h.getLoc();
        h.setLoc(newLoc);
        Cell oldCell = cells.get(oldLoc);
        Cell newCell = cells.get(newLoc);
        int index = oldCell.index(h);
        boolean stay = oldLoc.equals(newLoc);

        oldCell.removeHero(index,h);
        if (withGold) oldCell.takeGold(index);
        index = stay ? index+1 : 0;
        if (index>=newCell.count()) throw new IllegalStateException();
        newCell.addHero(index, h);
        if (withGold) newCell.addGold(index);
    }

    public void returnToShip(Hero h) {
        moveHero(h, ships[h.team()],false);
    }

    public String getId() {return id;}
    public View getView() {return new View(this);}
    public View getView(Hero selHero) {return new View(this, selHero);}

    private static Map<String,Game> gameMap = new HashMap<>();

    public static Game getGame(String id) {
        if (! gameMap.containsKey(id)) throw new RuntimeException("Game not found");
        return gameMap.get(id);
    }

    private static String generateId(int len) {
        StringBuilder builder = new StringBuilder(len);
        int count = 'z'-'a'+1;
        Random random = new Random();
        for (int i= 0; i<len; i++) {
            builder.append((char) ('a' + random.nextInt(count)));
        }
        return builder.toString();
    }
    public static Game newGame() {
        String id = generateId(16);
        Game game = new Game(id);
        gameMap.put(id,game);
        log.info("New game is created: " + id);
        return game;
    }
    public static void putGame(Game game) {
        gameMap.put(game.id, game);
    }

}
