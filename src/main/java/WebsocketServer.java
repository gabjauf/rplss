import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.subjects.PublishSubject;
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
    private HashMap<String, Observable> challenges;
    private PublishSubject<Pair<Player, Player>> gameCallback;
    private int port;

    public WebsocketServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
        players = new HashSet<>();
        lobby = new ArrayList<>();
        games = new HashMap<>();
        challenges = new HashMap<>();
        gameCallback =  PublishSubject.create();
        socketPlayerHashMap = new HashMap<>();
        gameCallback.subscribe(playerPair -> {
            Player player1 = playerPair.getKey();
            Player player2 = playerPair.getValue();
            games.remove(player1);
            games.remove(player2);
            broadcastLobbyJoined(player1);
            broadcastLobbyJoined(player2);
            broadcastLobby();
        }, error -> error.printStackTrace());
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
        broadcastLobbyLeft(toRemove);
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        Player currentPlayer = socketPlayerHashMap.get(conn);
        List<String> playersLogin = lobby.stream().map(p -> p.login).collect(Collectors.toList());
        Pair<String, String> command = socketHelper.messageBroker(message);
        switch (command.getKey()) {
            case "AUTH":
                if (isLoginValid(command.getValue())) {
                    currentPlayer.authTimer.dispose();
                    currentPlayer.login = command.getValue();
                    broadcastLobbyJoined(currentPlayer);
                    broadcastLobby();
                } else {
                    sendLobby(currentPlayer);
                }
                break;
            case "CHAT":
                broadcastChat(message);
                break;
            case "CHALLENGE":
                Observable challengeTimer = Observable.timer(5, TimeUnit.SECONDS);
                String[] challengeLogins = command.getValue().split(";");
                Player challengedPlayer = lobby.get(playersLogin.indexOf(challengeLogins[1]));
                challenges.put(challengeLogins[1], challengeTimer);
                challengedPlayer.socket.send(message);
                challengeTimer.doOnComplete(() -> {
                    challenges.remove(challengeLogins[1]);
                }).subscribe();
                break;
            case "CHALLENGE_KO":
                String[] challengeKoLogins = command.getValue().split(";");
                challenges.remove(challengeKoLogins[1]);
                break;
            case "CHALLENGE_OK":
                String[] logins = command.getValue().split(";");
                if (challenges.containsKey(logins[1])) {
                    // Not an incredible way of doing this, should probably change lobby datastructure
                    Player player1 = lobby.get(playersLogin.indexOf(logins[0]));
                    Player player2 = lobby.get(playersLogin.indexOf(logins[1]));
                    if (player1 != null && player2 != null) {
                        Game newGame = new Game(player1, player2, gameCallback);
                        games.put(player1, newGame);
                        games.put(player2, newGame);
                        broadcastLobbyLeft(player1);
                        broadcastLobbyLeft(player2);
                    }
                }
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
        List<String> playersLogin = players.stream().map(p -> p.login).collect(Collectors.toList());
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
        lobby.add(player);
        for (Player lobbyPlayer : lobby) {
            lobbyPlayer.socket.send(socketHelper.padRight("LOBBY_JOINED") + player.login + "\n");
        }
    }

    public void broadcastLobbyLeft(Player player) {
        lobby.remove(player);
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
