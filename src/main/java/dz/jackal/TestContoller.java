package dz.jackal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TestContoller {

    @MessageMapping("/test")
    @SendTo("/topic/jackal")
    public TestResponse action(TestRequest request) {
        System.out.println(request.name);
        return new TestResponse();
    }

    public static class TestRequest {
        public String name;
    }

    public static class TestResponse {

        public String id;
        public Inner inner;
        public Inner[] many;
        public List<Inner> manyList;
        public Icon i = Icon.SHIP;

        public TestResponse() {
            id = "iid";
            inner = new Inner();
            many = new Inner[3];
            for (int i = 0; i < many.length; i++) many[i] = new Inner();
            manyList = new ArrayList<>();
            for (int i = 0; i < 2; i++) manyList.add(new Inner());
        }

        public static class Inner {
            public String a;
            public int x;

            public Inner() {
                this.a = "This is a";
                this.x = 1357;
            }
        }

        public static enum TestE {
            A, BB, ABC
        }
    }
}

