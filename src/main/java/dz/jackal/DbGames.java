package dz.jackal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DbGames {

    private static Logger log = LoggerFactory.getLogger(DbGames.class);
    private final static Path root;

    static {
        String path = System.getenv("JACKAL_DB");
        if (path == null) {
            log.warn("JACKAL_DB env variable is not found. Will use default ./db folder");
            path = "./db";
        }
        try {
            root = Paths.get(path);
            Files.createDirectories(root);
        } catch (IOException e) {
            log.error("Can't set up db at path " + path, e);
            throw new RuntimeException("Can't set up db at path: " + path, e);
        }
    }

    private final static String PREFIX = "turn";
    private final static String EXT = "ser";
    private final static Pattern PATTERN = Pattern.compile(PREFIX + "([0-9]+)\\." + EXT);
    private static String getFileName(int turn) {
        return PREFIX + turn + "." + EXT;
    }

    private static Path getPath(String id, int turn) {
        return root.resolve(id).resolve(getFileName(turn));
    }

    public static String[] getIds() throws IOException {
        return Files.list(root)
                .filter(path -> !path.equals(root))
                .map(path -> path.getFileName().toString())
                .toArray(String[]::new);
    }

    private static void saveGameInternal(Game game) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(game);
        oos.close();
        out.close();

        Path path = getPath(game.getId(), game.getTurn());
        Files.write(path, out.toByteArray());
        log.info("Game saved to " + path);
    }

    public static void trimGames(String id, int turn) throws IOException {
        Path[] toDelete = Files.list(root.resolve(id))
                .filter(path -> {
                    Matcher matcher = PATTERN.matcher(path.getFileName().toString());
                    if (!matcher.matches()) return false;
                    return Integer.parseInt(matcher.group(1)) > turn;
                }).toArray(Path[]::new);
        for(Path f: toDelete) {
            log.warn("Deleting " + f + " because of re-writing");
            Files.delete(f);
        }
    }

    public static void saveGame(Game game) throws IOException {
        String id = game.getId();
        Path folder = root.resolve(id);
        Files.createDirectories(folder);

        int turn = game.getTurn();
        trimGames(id, turn-1);
        saveGameInternal(game);
    }

    public static int getLastTurn(String id) throws IOException {
        Path folder = root.resolve(id);
        return Files.list(folder)
                    .map(path -> {
                        Matcher matcher = PATTERN.matcher(path.getFileName().toString());
                        if (!matcher.matches()) return 0;
                        return Integer.parseInt(matcher.group(1));
                    })
                    .max(Integer::compare).orElse(0);

    }

    public static long getTime(String id, int turn) throws IOException {
        return Files.getLastModifiedTime(getPath(id, turn)).toMillis();
    }

    public static void setTime(String id, int turn, long millis) throws IOException {
        Files.setLastModifiedTime(getPath(id, turn), FileTime.fromMillis(millis));
    }


    public static String[] getTeamNames(String id) throws IOException {
        int turn = getLastTurn(id);
        Game game = loadGame(id, turn);
        String[] names = new String[4];
        for (int team=0; team<4; team++) {
            names[team] = game.getTeamName(team);
        }
        return names;
    }

    public static Game loadGame(String id, int turn) throws IOException {
        Path path = getPath(id, turn);
        ByteArrayInputStream inp = new ByteArrayInputStream(Files.readAllBytes(path));
        ObjectInputStream ois = new ObjectInputStream(inp);
        try {
            Game game = (Game) ois.readObject();
            ois.close();
            inp.close();
            return game;
        } catch (ClassNotFoundException e) {
            log.error("Can't load game id " + id + "; turn " + turn, e);
            throw new IOException("Can't deserialize game at " + path, e);
        }
    }

    public static void updateGame(String id, String[] names, int[] friends) throws IOException {
        int last = getLastTurn(id);
        for (int turn = 0; turn<=last; turn++) {
            Game game = loadGame(id, turn);
            game.setTeamNames(names);
            game.setFriends(friends);

            long millis = getTime(id, turn);
            saveGameInternal(game);
            setTime(id, turn, millis);
        }
    }

}
