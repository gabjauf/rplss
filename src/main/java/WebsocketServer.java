import io.reactivex.Observable;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import javax.persistence.Tuple;
import java.io.IOException;
import java.lang.reflect.Array;
import io.reactivex.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.util.Pair;

public class WebsocketServer extends WebSocketServer {

    private Set<Player> players;
    private HashMap<WebSocket, Player> socketPlayerHashMap;
    private List<Player> lobby;
    private HashMap<Player, Game> games;
    private int port;

    public WebsocketServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
        players = new HashSet<>();
        lobby = new ArrayList<>();
        games = new HashMap<>();
        socketPlayerHashMap = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        try {
            Player player = new Player(conn);
            players.add(player);
            socketPlayerHashMap.put(conn, player);
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Player toRemove = socketPlayerHashMap.get(conn);
        players.remove(toRemove);
        lobby.remove(toRemove);
        broadcastLobbyLeft(toRemove);
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        Player currentPlayer = socketPlayerHashMap.get(conn);
        Pair<String, String> command = socketHelper.messageBroker(message);
        switch (command.getKey()) {
            case "AUTH":
                if (isLoginValid(command.getValue())) {
                    currentPlayer.authTimer.dispose();
                    currentPlayer.login = command.getValue();
                    lobby.add(currentPlayer);
                    broadcastLobbyJoined(currentPlayer);
                    broadcastLobby();
                    if (lobby.size() > 1) {
                        Player player1 = lobby.get(0);
                        Player player2 = lobby.get(1);
                        Game newGame = new Game(player1, player2);
                        games.put(player1, newGame);
                        games.put(player2, newGame);
                        broadcastLobbyLeft(player1);
                        broadcastLobbyLeft(player2);
                    }
                } else {
                    sendLobby(currentPlayer);
                }
                break;
            case "CHAT":
                broadcastChat(message);
                break;
            case "MOVE":
                currentPlayer.incommingMessage.onNext(command);
                break;
            default:
                System.out.println("Invalid command: " + command.getKey());
                break;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();

        if (conn != null) {
            Player toRemove = socketPlayerHashMap.get(conn);
            players.remove(toRemove);
            // do some thing if required
            System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        }

    }

    @Override
    public void onStart() {
        System.out.println("rplss Server is Running on port " + port);
    }

    public boolean isLoginValid(String login) {
        List<String> playersLogin = lobby.stream().map(p -> p.login).collect(Collectors.toList());
        return !playersLogin.contains(login);
    }

    public void broadcastLobby() {
        String[] playersLogin = lobby.stream().map(p -> p.login).toArray(String[]::new);
        String joinedLogins = String.join("_", playersLogin);
        for (Player player : lobby) {
            player.socket.send(socketHelper.padRight("LOBBY") + joinedLogins + "\n");
        }
    }

    public void sendLobby(Player player) {
        String[] playersLogin = lobby.stream().map(p -> p.login).toArray(String[]::new);
        String joinedLogins = String.join("_", playersLogin);
        player.socket.send(socketHelper.padRight("LOBBY") + joinedLogins + "\n");
    }

    public void broadcastLobbyJoined(Player player) {
        for (Player lobbyPlayer : lobby) {
            lobbyPlayer.socket.send(socketHelper.padRight("LOBBY_JOINED") + player.login + "\n");
        }
    }

    public void broadcastLobbyLeft(Player player) {
        for (Player lobbyPlayer : lobby) {
            lobbyPlayer.socket.send(socketHelper.padRight("LOBBY_LEFT") + player.login + "\n");
        }
    }

    public void broadcastChat(String message) {
        for (Player player : lobby) {
            player.socket.send(message);
        }
    }
}
