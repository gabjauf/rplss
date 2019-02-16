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
        this.login = login;
        /*
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            output = new PrintWriter(socket.getOutputStream(), true);
            handshake();
            output.println("WELCOME " + login);
            output.println("MESSAGE Waiting for opponent to connect");
        } catch (Exception e) {
            System.out.println("Player died: " + e);
        }
        */
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public void run() {
        try {
            // The thread is only started after everyone connects.
            output.println("MESSAGE All players connected");


            String command;
            // Repeatedly get commands from the client and process them.
            while ((command = input.readLine()) != null) {
                System.out.println("command: " + command);
            }
        } catch (IOException e) {
            System.out.println("Player died: " + e);
        } finally {
            socket.close();
        }
    }

    private void handshake() {
        try {
            String line;
            String key = "";
            while (true) {
                line = input.readLine();
                if (line.startsWith("Sec-WebSocket-Key: ")) {
                    key = line.split(" ")[1];
                    System.out.println("'" + key + "'");
                }
                if (line == null || line.isEmpty())
                    break;
            }
            output.println("HTTP/1.1 101 Switching Protocols");
            output.println("Upgrade: websocket");
            output.println("Connection: Upgrade");
            output.println("Sec-WebSocket-Accept: " + encode(key));
            output.println();
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encode(String key) throws Exception {
        key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        byte[] bytes = MessageDigest.getInstance("SHA-1").digest(key.getBytes());
        return DatatypeConverter.printBase64Binary(bytes);
    }

}
