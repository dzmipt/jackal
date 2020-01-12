package dz.jackal;

import dz.jackal.cell.*;

import java.util.*;

public class GameInitializer {

    private static Map<Loc,Cell> cells;
    private static List<Loc> locs;


    public static Map<Loc,Cell> init() {
         initSea();
        locs = new ArrayList<>(Loc.ALL);
        locs.removeAll(cells.keySet());

        next(3, () -> new ArrowMoveCell(Move.N) );
        next(3, () -> new ArrowMoveCell(Move.NE) );
        next(3, () -> new ArrowMoveCell(Move.N, Move.S) );
        next(3, () -> new ArrowMoveCell(Move.NE, Move.SW) );
        next(3, () -> new ArrowMoveCell(Move.NW, Move.E, Move.S) );
        next(3, () -> new ArrowMoveCell(Move.N, Move.E, Move.S, Move.W) );
        next(3, () -> new ArrowMoveCell(Move.NE, Move.SE, Move.SW, Move.NW) );

        next(2, Knight::new);
        next(6, Ice::new);

        next(5, () -> new Cell(Icon.JUNGLE2, 2));
        next(4, () -> new Cell(Icon.DESERT, 3));
        next(2, () -> new Cell(Icon.SWAMP, 4));
        next(1, () -> new Cell(Icon.MOUNTAIN, 5));

        next(1, () -> new Cell(Icon.CANNIBAL));
        next(2, () -> new Cell(Icon.FORT));
        next(1, () -> new Cell(Icon.WOMAN));
        next(2, () -> new Cell(Icon.BALLOON));
        next(2, Cannon::new);
        next( 3, () -> new Cell(Icon.TRAP));
        next( 4, () -> new Cell(Icon.CROCODILE));

        next(4, Cave::new);
        next(1, Earthquake::new);
        next(1, Den::new);
        next(2, TeeHee::new);

        next(5, () -> goldCell(1));
        next(5, () -> goldCell(2));
        next(3, () -> goldCell(3));
        next(2, () -> goldCell(4));
        next(1, () -> goldCell(5));

        next(1, () -> rumCell(3));
        next(2, () -> rumCell(2));
        next(3, () -> rumCell(1));

        initHero(HeroId.BENGUNN_ID);
        initHero(HeroId.FRIDAY_ID);
        initHero(HeroId.MISSIONER_ID);

        for(Loc loc:locs) {
            cells.put(loc, new Land());
        }

        return cells;
    }

    private static void initHero(HeroId id) {
        Cell cell = new Cell(Icon.LAND);
        Loc loc = nextCell(cell);
        Hero hero = id == HeroId.MISSIONER_ID ? new Missioner(loc) : new Hero(id, loc);
        cell.addHero(0, hero);
    }

    private static Cell rumCell(int count) {
        Cell cell = new Cell(Icon.LAND);
        cell.setRum(count);
        return cell;
    }

    private static Cell goldCell(int count) {
        Cell cell = new Cell(Icon.LAND);
        for (int i=0; i<count; i++){
            cell.addGold(0);
        }
        return cell;
    }

    private static void next(int count, Initializer initializer) {
        for (int i=0; i<count; i++) {
            nextCell(initializer.init());
        }
    }

    private static Loc nextCell(Cell cell) {
        int index = Game.random.nextInt(locs.size());
        Loc loc = locs.remove(index);
        cells.put(loc, cell);
        return loc;
    }


    private static void initSea() {
        cells = new HashMap<>();
        for(int i=1;i<12;i++) {
            cells.put(new Loc(0,i), getSea());
            cells.put(new Loc(12,i), getSea());
            cells.put(new Loc(i,0), getSea());
            cells.put(new Loc(i,12), getSea());
        }
        cells.put(new Loc(0,0), getSea());
        cells.put(new Loc(1,1), getSea());
        cells.put(new Loc(0,12), getSea());
        cells.put(new Loc(1,11), getSea());
        cells.put(new Loc(12,0), getSea());
        cells.put(new Loc(11,1), getSea());
        cells.put(new Loc(12,12), getSea());
        cells.put(new Loc(11,11), getSea());

        Loc[] ships = new Loc[] {new Loc(0,6), new Loc(6, 12),
                new Loc(12,6), new Loc(6,0)};
        for(int team=0; team<4; team++) {
            Cell ship = new Ship(team);
            ship.open();
            cells.put(ships[team], ship);
        }
        for(HeroId id:HeroId.ALL) {
            if (! id.pirate()) continue;
            int team = id.team();
            Hero hero = new Hero(id, ships[team]);
            cells.get(ships[team]).addHero(0, hero);
        }
    }

    private static Cell getSea() {
        Cell cell = new Cell(Icon.SEA,1);
        cell.open();
        return cell;
    }


    private interface Initializer {
        Cell init();
    }


    private static MoveCell[] moves = new MoveCell[] {
            /*new KnightCell(),
            new IceCell(),*/
            new ArrowMoveCell(Move.N),
            /*new ArrowMoveCell(random, Move.NW),
            new ArrowMoveCell(random, Move.E, Move.W),
            new ArrowMoveCell(random, Move.NW, Move.SE),
            new ArrowMoveCell(random, Move.E, Move.W, Move.N, Move.S),
            new ArrowMoveCell(random, Move.NW, Move.NE, Move.SW, Move.SE),
            new ArrowMoveCell(random, Move.NW, Move.E, Move.S)*/
    };

    public static Map<Loc,Cell> initTest() {
        Map<Loc,Cell> cells = new HashMap<>();
        for(Loc loc: Loc.ALL) {
            int row = loc.row();
            int col = loc.col();
            Icon icon;
            if (((row == 0 || row == 12) && col == 6) ||
                    ((col == 0 || col == 12) && row == 6)) icon = Icon.SHIP;
            else if ((row == 0 || row == 12 || col == 0 || col == 12) ||
                    ((row == 1 || row == 11) && (col == 1 || col == 11))) icon = Icon.SEA;
            else /*if (random.nextInt(10)>-6)*/ icon = Icon.LAND;
            //else icon = Icon.MOVE; //icon = Icon.LAND; //Icon.MOUNTAIN;//Icon.LAND;
            //else icon = Icon.MOUNTAIN;

            Cell cell;
            if (icon == Icon.MOUNTAIN) {
                cell = new Cell(icon, 5);
                cell.addGold(0);
                cell.addGold(1);
            } else if (icon == Icon.LAND) {
                cell = new Land();
                int cnt = Game.random.nextInt(6);
                for (int i=0;i<cnt;i++) cell.addGold(0);
                cell.setRum(Game.random.nextInt(4));
            } else /*if (icon == Icon.MOVE) {
                cell = moves[random.nextInt(moves.length)].duplicate();
            } else*/ {
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
        ArrowMoveCell move82 = new ArrowMoveCell(Move.W);
        move82.setMoves(Move.W);


        cells.put(new Loc(6, 11), new Cell(Icon.LAND, 1));
        cells.put(new Loc(6,10), new Cell(Icon.WOMAN, 1));
        cells.put(new Loc(6,9), move4);
        cells.put(new Loc(5,9), move41);
        cells.put(new Loc(5,10), move8);
        cells.put(new Loc(4,9), move81);
        cells.put(new Loc(7,1), move82);

        cells.put(new Loc(10, 6), new Cell(Icon.FORT, 1));
        cells.put(new Loc(6,6), new Cell(Icon.BALLOON, 1));

        cells.put(new Loc(7, 10), new Cell(Icon.TRAP, 1));

        Cannon cannon = new Cannon();
        cannon.setMove(Move.W);
        cells.put(new Loc (10, 5), cannon);

        cells.put(new Loc(4,11), new Cell(Icon.CANNIBAL, 1));

        cells.put(new Loc(11,2),new Ice());
        cells.put(new Loc(9,3), new Knight());

        cells.put(new Loc(1,5), new Cell(Icon.CROCODILE));

        ArrowMoveCell move = new ArrowMoveCell(Move.NE, Move.SW);
        move.setMoves(Move.NE, Move.SW);
        cells.put(new Loc(7,11), move);
        cells.put(new Loc(8,10), new Cell(Icon.CROCODILE));

        move = new ArrowMoveCell(Move.W);
        move.setMoves(Move.W);
        cells.put(new Loc(11,5), move);
        cells.put(new Loc(11,4), new Cell(Icon.CROCODILE));

        cells.put(new Loc(1,8), new Cave());
        cells.put(new Loc(5,1), new Cave());
        cells.put(new Loc(4,8), new Cave());
        cells.put(new Loc(11,8), new Cave());

        cells.put(new Loc(7, 2), new Earthquake());
        cells.put(new Loc(7, 3), new Den());
        cells.put(new Loc(10, 7), new TeeHee());

        Loc[] ships = new Loc[] {new Loc(0,6), new Loc(6, 12),
                new Loc(12,6), new Loc(6,0)};

        for(int team=0; team<4; team++) {
            Ship shipCell = new Ship(team);
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
                cells.put(loc, new Cell(Icon.LAND, 1));
            }
            Hero hero = heroId.equals(HeroId.MISSIONER_ID) ? new Missioner(loc) : new Hero(heroId, loc);
            cells.get(loc).addHero(0,hero);
        }

//        for (Cell cell:cells.values()) cell.open();
        return cells;
    }
}
