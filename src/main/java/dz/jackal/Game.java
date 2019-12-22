package dz.jackal;

import dz.jackal.cell.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {
    private final static long serialVersionUID = 2;
    private final static Logger log = LoggerFactory.getLogger(Game.class);

    private String id;
    private Map<Loc, Cell> cells = new HashMap<>();
    private Loc[] ships;
    private Map<HeroId, Hero> heroes = new HashMap<>();
    private int currentTeam = 0;
    private int turn = 0;
    private boolean startTurn = true;

    private static MoveCell[] moves = new MoveCell[] {
            /*new KnightCell(),
            new IceCell(),*/
            new ArrowMoveCell(Move.N),
            /*new ArrowMoveCell(Move.NW),
            new ArrowMoveCell(Move.E, Move.W),
            new ArrowMoveCell(Move.NW, Move.SE),
            new ArrowMoveCell(Move.E, Move.W, Move.N, Move.S),
            new ArrowMoveCell(Move.NW, Move.NE, Move.SW, Move.SE),
            new ArrowMoveCell(Move.NW, Move.E, Move.S)*/
    };

    private Game(String id) {
        this.id = id;

        Random random = new Random();

        for(Loc loc: Loc.ALL) {
            int row = loc.row();
            int col = loc.col();
            Icon icon;
            if (((row == 0 || row == 12) && col == 6) ||
                    ((col == 0 || col == 12) && row == 6)) icon = Icon.SHIP;
            else if ((row == 0 || row == 12 || col == 0 || col == 12) ||
                    ((row == 1 || row == 11) && (col == 1 || col == 11))) icon = Icon.SEA;
            else if (random.nextInt(10)>5) icon = Icon.LAND;
            //else icon = Icon.MOVE; //icon = Icon.LAND; //Icon.MOUNTAIN;//Icon.LAND;
            else icon = Icon.MOUNTAIN;

            Cell cell;
            if (icon == Icon.MOUNTAIN) {
                cell = new Cell(icon, 5);
                cell.addGold(0);
                cell.addGold(1);
            } else if (icon == Icon.LAND) {
                cell = new Cell(icon, 1);
                int cnt = random.nextInt(4);
                for (int i=0;i<cnt;i++) cell.addGold(0);
                cell.setRum(random.nextInt(4));
            } else if (icon == Icon.MOVE) {
                cell = moves[random.nextInt(moves.length)].duplicate();
            } else {
                cell = new Cell(icon);
                cell.open();
            }

            cells.put(loc, cell);
        }

        ships = new Loc[] {new Loc(0,6), new Loc(6, 12),
                             new Loc(12,6), new Loc(6,0)};

        for(int team=0; team<4; team++) {
            ShipCell shipCell = new ShipCell(team);
            shipCell.open();
            cells.put(ships[team], shipCell);
        }
        for(HeroId heroId : HeroId.ALL) {
            int team;
            Loc loc;
            if (heroId.pirate()) {
                team = heroId.team();
                loc = ships[team];
            } else {
                if (heroId.benGunn()) loc = new Loc(6,5);
                else if (heroId.friday()) loc = new Loc(6,6);
                else if (heroId.missioner()) loc = new Loc(6,7);
                else throw new IllegalStateException();
                team = -1;
                cells.put(loc, new Cell(Icon.LAND, 1));
            }
            Hero hero = new Hero(heroId, team, loc);
            heroes.put(heroId, hero);
            getCell(loc).addHero(0,hero);
        }
    }

    public Cell getCell(Loc loc) {return cells.get(loc);}
    public Hero getHero(HeroId heroId) {return heroes.get(heroId);}

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

    public void addRum(int team, int rum) {
        Cell ship = getTeamShip(team);
        ship.setRum(ship.countRum() + rum);
    }

    public boolean enemy(int team1, int team2) {
        return (team1+team2) % 2 == 1;
        //return team1!=team2;
    }

    public boolean enemy(Hero p1, Hero p2) {
        return enemy(p1.team(), p2.team());
    }

    public boolean hasEnemy(Hero hero, Collection<Hero> heroes) {
        return heroes.stream()
                .anyMatch(p -> enemy(hero,p));
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
