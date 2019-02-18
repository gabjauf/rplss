import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.java_websocket.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import io.reactivex.*;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import javafx.util.Pair;

public class Player extends Thread {
    String login;
    Player opponent;
    WebSocket socket;
    BufferedReader input;
    PrintWriter output;
    Subscription authReq;
    Disposable authTimer;
    public PublishSubject<Pair<String, String>> incommingMessage;

    public Player(WebSocket socket) throws IOException {
        this.socket = socket;
        this.login = "";
        this.incommingMessage = PublishSubject.create();

    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public void run() {
        try {
            incommingMessage.subscribe(message -> System.out.println(message.getKey()));
            socket.send("AUTH_REQ--------\n");
            authTimer = Observable
                    .interval(5, TimeUnit.SECONDS)
                    .doOnError(e -> e.printStackTrace())
                    .subscribe( time -> sendAuthReq());
        } catch (Exception e) {
            System.out.println("Player died: " + e);
        }
    }

    public void sendAuthReq() {
        System.out.println("Sending AUTH_REQ");
        socket.send("AUTH_REQ--------\n");
    }



}
