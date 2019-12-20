package dz.jackal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Application {

    public static String[] ids;

    public static void main(String[] args) throws IOException {
        ids = DbGames.getIds();
        SpringApplication.run(Application.class, args);
    }
}
