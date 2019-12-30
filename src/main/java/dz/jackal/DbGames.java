package dz.jackal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static void saveGame(Game game) throws IOException {
        Path folder = root.resolve(game.getId());
        Files.createDirectories(folder);

        int turn = game.getTurn();
        Path[] toDelete = Files.list(folder)
                            .filter(path -> {
                                Matcher matcher = PATTERN.matcher(path.getFileName().toString());
                                if (!matcher.matches()) return false;
                                return Integer.parseInt(matcher.group(1)) >= turn;
                            }).toArray(Path[]::new);
        for(Path f: toDelete) {
            log.warn("Deleting " + f + " because of re-writing");
            Files.delete(f);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(game);
        oos.close();
        out.close();

        Path path = folder.resolve(getFileName(turn));
        Files.write(path, out.toByteArray());
        log.info("Game saved to " + path);
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
            throw new IOException("Can't deserialize game at " + path, e);
        }
    }

}
