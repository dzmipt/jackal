package dz.jackal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NextTeamController extends GameController {
    @MessageMapping("/nextTeam")
    @SendTo("/jackal/view")
    public View go(Request request) {
        return super.action(request);
    }

    @Override
    protected View processAction(Request request) {
        nextTurn();
        return getView(null);
    }
}
