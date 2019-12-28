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
            else if (random.nextInt(10)>-6) icon = Icon.LAND;
            else icon = Icon.MOVE; //icon = Icon.LAND; //Icon.MOUNTAIN;//Icon.LAND;
            //else icon = Icon.MOUNTAIN;

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
        cells.put(new Loc(2,6),new Cell(Icon.MOUNTAIN, 5));
        cells.put(new Loc(2,5),new Cell(Icon.SWAMP, 4));
        cells.put(new Loc(2,4),new Cell(Icon.DESERT, 3));
        cells.put(new Loc(2,3),new Cell(Icon.JUNGLE2, 2));

        ArrowMoveCell move4 = new ArrowMoveCell(Move.E);
        move4.setMoves(Move.E);
        ArrowMoveCell move41 = new ArrowMoveCell(Move.E);
        move41.setMoves(Move.E);
        ArrowMoveCell move8 = new ArrowMoveCell(Move.W);
        move8.setMoves(Move.W);
        ArrowMoveCell move81 = new ArrowMoveCell(Move.W);
        move81.setMoves(Move.W);

        cells.put(new Loc(6, 11), new Cell(Icon.LAND, 1));
        setWoman(new Loc(6,10));
        cells.put(new Loc(6,9), move4);
        cells.put(new Loc(5,9), move41);
        cells.put(new Loc(5,10), move8);
        cells.put(new Loc(4,9), move81);

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
                if (heroId.benGunn()) loc = new Loc(1,7);
                else if (heroId.friday()) loc = new Loc(5,11);
                else if (heroId.missioner()) loc = new Loc(11,7);
                else throw new IllegalStateException();
                team = -1;
                cells.put(loc, new Cell(Icon.LAND, 1));
            }
            Hero hero = heroId.equals(HeroId.MISSIONER_ID) ? new Missioner(loc) : new Hero(heroId, team, loc);
            heroes.put(heroId, hero);
            getCell(loc).addHero(0,hero);
        }
    }

    private void setWoman(Loc loc) {
        woman = new Cell(Icon.WOMAN, 1);
        cells.put(loc, woman);
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
