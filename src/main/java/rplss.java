import java.io.IOException;
import java.net.ServerSocket;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;


public class rplss {


    public static void main(String[] args) throws IOException {

        int port = 4242;

        WebsocketServer server = new WebsocketServer(port) ;
        server.run();

            // Game game = new Game(player1, player2);


    }

}
