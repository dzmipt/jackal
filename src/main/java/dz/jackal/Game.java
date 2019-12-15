package dz.jackal;

import dz.jackal.cell.*;

import java.util.*;

public class Game {

    private String id;
    private Map<Loc, Cell> cells = new HashMap<>();
    private Loc[] ships;
    private Map<PirateId,Pirate> pirates = new HashMap<>();

    private MoveCell[] moves = new MoveCell[] {
            //new KnightCell(),
            new IceCell(),
            new ArrowMoveCell(Move.N),
            new ArrowMoveCell(Move.NW),
/*            new ArrowMoveCell(Move.E, Move.W),
            new ArrowMoveCell(Move.NW, Move.SE),*/
            /*new SimpleMoveCell(Move.E, Move.W, Move.N, Move.S),
            new SimpleMoveCell(Move.NW, Move.NE, Move.SW, Move.SE),
            new SimpleMoveCell(Move.NW, Move.E, Move.S)*/
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
            else if (random.nextInt(2)==0) icon = Icon.LAND;
            else icon = Icon.MOVE; //icon = Icon.LAND; //Icon.MOUNTAIN;//Icon.LAND;

            Cell cell;
            if (icon == Icon.MOUNTAIN) {
                cell = new Cell(icon, 5);
                cell.addGold(0);
                cell.addGold(0);
            } else if (icon == Icon.LAND) {
                cell = new Cell(icon, 1);
                cell.addGold(0);
                cell.addGold(0);
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
        for(PirateId pirateId: PirateId.ALL) {
            Loc loc = ships[pirateId.team()];
            Pirate pirate = new Pirate(pirateId, loc);
            pirates.put(pirateId, pirate);
            cells.get(loc).addHero(0,pirate);
        }
    }

    public Cell getCell(Loc loc) {return cells.get(loc);}
    public Pirate getPirate(PirateId pirateId) {return pirates.get(pirateId);}

    public boolean enemy(int team1, int team2) {
        return team1!=team2;
    }

    public boolean enemy(Pirate p1, Pirate p2) {
        return enemy(p1.team(), p2.team());
    }

    public boolean hasEnemy(Pirate pirate, Collection<Pirate> heroes) {
        return heroes.stream()
                .anyMatch(p -> enemy(pirate,p));
    }

    public void moveShip(int team, Loc newLoc) {
        Loc oldLoc = ships[team];
        if (oldLoc.equals(newLoc)) return;
        ships[team] = newLoc;
        Cell newCell = cells.get(newLoc);
        Cell oldCell = cells.get(oldLoc);
        cells.put(newLoc,oldCell);
        cells.put(oldLoc,newCell);

        oldCell.heroes(0).forEach(p->p.setLoc(newLoc));
    }

    public void movePirate(Pirate p, Loc newLoc, boolean withGold) {
        Loc oldLoc = p.getLoc();
        p.setLoc(newLoc);
        Cell oldCell = cells.get(oldLoc);
        Cell newCell = cells.get(newLoc);
        int index = oldCell.index(p);
        boolean stay = oldLoc.equals(newLoc);
        //if (stay && ! oldLoc.equals(newLoc)) throw new IllegalArgumentException();

        oldCell.removeHero(index,p);
        if (withGold) oldCell.takeGold(index);
        index = stay ? index+1 : 0;
        if (index>=newCell.count()) throw new IllegalStateException();
        newCell.addHero(index, p);
        if (withGold) newCell.addGold(index);
    }

    public void returnToShip(Pirate p) {
        movePirate(p, ships[p.team()],false);
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
        return game;
    }
}
