package dz.jackal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Controller
public class InitController {
    @MessageMapping("/init")
    @SendTo("/jackal/view")
    public View action(InitRequest request) {
        return Game.newGame().getView();
    }
    public static class InitRequest{}
}
