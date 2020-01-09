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
    private String[] teamNames;
    private int[] friends;

    private Map<Loc, Cell> cells = new HashMap<>();
    private Cell woman;
    private List<Loc> caveLocs;
    private Loc[] ships = new Loc[4];
    private Map<HeroId, Hero> heroes = new HashMap<>();
    private int currentTeam = 0;
    private int turn = 0;
    private boolean startTurn = true;

    public final static Random random = new Random();

    private Game(String id, String[] teamNames, int[] friends) {
        this.id = id;
        setTeamNames(teamNames);
        setFriends(friends);

//        cells = GameInitializer.init();
        cells = GameInitializer.initTest();
        afterCellsInit();
    }

    private void afterCellsInit() {
        caveLocs = new ArrayList<>();
        for(Map.Entry<Loc,Cell> entry: cells.entrySet()) {
            Loc loc = entry.getKey();
            Cell cell = entry.getValue();
            int count = cell.count();
            for (int index=0; index<count; index++) {
                for(Hero hero:cell.heroes(index)) {
                    heroes.put(hero.id(), hero);
                    hero.setLoc(loc);
                }
            }
            if (cell.ship()) {
                ships[((Ship)cell).team()] = loc;
            } else if (cell.woman()) {
                woman = cell;
            } else if (cell.cave()) {
                caveLocs.add(loc);
            }
        }
    }

    void setTeamNames(String[] teamNames) {
        this.teamNames = teamNames;
    }

    void setFriends(int[] friends) {
        this.friends = friends;
    }

    public Cell woman() {
        return woman;
    }

    public List<Loc> caveLocs() {
        return caveLocs;
    }

    public Cell getCell(Loc loc) {return cells.get(loc);}
    public Hero getHero(HeroId heroId) {return heroes.get(heroId);}
    public Missioner getMissioner() {return (Missioner) getHero(HeroId.MISSIONER_ID);}


    public Loc getTeamShipLoc(int team) {
        return ships[team];
    }
    public Ship getTeamShip(int team) {
        return (Ship) getCell(ships[team]);
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
        if (team1 == -1 || team2 == -1) return false;
        return friends[team1] != friends[team2];
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
        Cell oldCell = cells.get(oldLoc);
        Cell newCell = cells.get(newLoc);
        if (newLoc.equals(oldLoc) && !newCell.multiStep()) {
            log.warn("moveHero: the hero " + h + " is alread at " + newLoc);
            return;
        }
        h.setLoc(newLoc);
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
        Loc newLoc = getTeamShipLoc(h.team());
        if (h.getLoc().equals(newLoc)) {
            log.warn("returnToShip: Hero " + h + " is already on the ship");
            return;
        }
        moveHero(h, newLoc,false);
    }

    public void earthquake() {
        List<Loc> locs = new ArrayList<>();
        List<Cell> newCells = new ArrayList<>();
        for (Map.Entry<Loc,Cell> entry: cells.entrySet()) {
            Loc loc = entry.getKey();
            Cell cell = entry.getValue();
            if (!cell.land()) continue;
            if (!cell.closed()) {
                boolean take = true;
                int count = cell.count();
                for (int index=0; index<count; index++) {
                    if (cell.gold(index)>0) take = false;
                    if (cell.heroes(index).size()>0) take = false;
                }
                if (!take) continue;
            }
            locs.add(loc);
            cell.random();
            newCells.add(cell);
        }

        for(Loc loc:locs) {
            Cell cell = newCells.remove(Game.random.nextInt(newCells.size()));
            cells.put(loc,cell);
        }

        afterCellsInit();
    }

    public String getId() {return id;}
    public String getTeamName(int team) {
        return teamNames[team];
    }

    private static Map<String,Game> gameMap = new HashMap<>();

    public static Game getGame(String id) {
        if (! gameMap.containsKey(id)) throw new RuntimeException("Game not found");
        return gameMap.get(id);
    }

    private static String generateId(int len) {
        StringBuilder builder = new StringBuilder(len);
        int count = 'z'-'a'+1;
        for (int i= 0; i<len; i++) {
            builder.append((char) ('a' + random.nextInt(count)));
        }
        return builder.toString();
    }
    public static Game newGame(String[] teamNames, int[] friends) {
        String id = generateId(16);
        Game game = new Game(id, teamNames, friends);
        gameMap.put(id,game);
        log.info("New game is created: " + id);
        return game;
    }
    public static void putGame(Game game) {
        gameMap.put(game.id, game);
    }

}
