import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.lang.model.type.ArrayType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Game extends Thread {

    Player player1;
    Player player2;

    public int moveCount = 0;

    public int scorePlayer1 = 0;
    public int scorePlayer2 = 0;

    String player1Move;
    String player2Move;

    Disposable gameTimer;


    Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        player1.incommingMessage.subscribe(message -> {
            System.out.println("InGame1: " + message.getKey());
            switch (message.getKey()) {
                case "MOVE":
                    player1Move = message.getValue();
                    break;
            }
        });
        player2.incommingMessage.subscribe(message -> {
            System.out.println("InGame1: " + message.getKey());
            switch (message.getKey()) {
                case "MOVE":
                    player2Move = message.getValue();
                    break;
            }
        });
        this.player1.sendAuthReq();
        this.player2.sendAuthReq();
        Observable.timer(5, TimeUnit.SECONDS).subscribe(time -> this.run());
    }

    final Map<String, List<String>> gestureWinTable = Map.of(
            "rock", List.of("scissor", "lizard"),
            "paper", List.of("rock", "spock"),
            "scissor",  List.of("paper", "lizard"),
            "lizard",  List.of("spock", "paper"),
            "spock",  List.of("rock", "scissor")
    );

    public int getWinner(String gesture1, String gesture2) {
        List<String> player1beats = gestureWinTable.get(gesture1);
        List<String> player2beats = gestureWinTable.get(gesture2);
        if (player1beats.contains(gesture2))
            return 1;
        if (player2beats.contains(gesture1))
            return 2;
        return 0; // draw
    }

    public void updateGame(int winner) {
        this.moveCount++;
        if (winner == 1) {
            this.scorePlayer1++;
        }
        if (winner == 2) {
            this.scorePlayer2++;
        }
    }


    public synchronized boolean legalMove(String gesture) {
        if (gesture == null) {
            return false;
        }
        List<String> validGesture = List.of("rock", "paper", "scissor", "lizard", "spock");
        return validGesture.contains(gesture);
    }

    public void run() {
        try {
            Observable.interval(10, TimeUnit.SECONDS)
                    .doOnError(e -> e.printStackTrace())
                    .takeUntil(e -> moveCount >= 5)
                    .subscribe(time -> {
                        String statusMessage;
                        if (!legalMove(player1Move)) {
                            updateGame(2);
                            statusMessage = statusMessage(2);

                        } else if (!legalMove(player2Move)) {
                            updateGame(1);
                            statusMessage = statusMessage(1);

                        } else {
                            int winner = getWinner(player1Move, player2Move);
                            updateGame(winner);
                            statusMessage = statusMessage(winner);
                        }
                        String moveReq = socketHelper.padRight("MOVE_REQ") + "\n";
                        player1.socket.send(statusMessage);
                        player2.socket.send(statusMessage);
                        player1.socket.send(moveReq);
                        player2.socket.send(moveReq);
                    });
        } catch (Exception e) {
            System.out.println("Player died: " + e);
        }
    }

    private String statusMessage(int winner) {
        if (winner == 1) {
            return socketHelper.padRight("STATUS") + player1.login + ";" + scorePlayer1 + ";" + scorePlayer2 + "\n";
        }
        if (winner == 2) {
            return socketHelper.padRight("STATUS") + player2.login + ";" + scorePlayer2 + ";" + scorePlayer1 + "\n";
        }
        else {
            return socketHelper.padRight("STATUS") + ";;\n";
        }
    }

}
