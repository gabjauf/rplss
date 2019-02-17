import java.io.IOException;
import java.util.Map;


public class rplss {


    public static void main(String[] args) throws IOException {

        Map<String, String> env = System.getenv();
        int port = env.containsKey("RPSLS_HOST") ?
            Integer.parseInt(env.get("RPSLS_HOST"))
            : 4242;




        WebsocketServer server = new WebsocketServer(port) ;
        server.run();

            // Game game = new Game(player1, player2);


    }

}
