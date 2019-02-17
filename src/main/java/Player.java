import org.java_websocket.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;

public class Player extends Thread {
    String login;
    Player opponent;
    WebSocket socket;
    BufferedReader input;
    PrintWriter output;

    public Player(WebSocket socket) throws IOException {
        this.socket = socket;
        try {
            socket.send("AUTH_REQ--------\n");
        } catch (Exception e) {
            System.out.println("Player died: " + e);
        }
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public void run() {

    }

}
