import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;

import javax.lang.model.type.ArrayType;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javafx.util.Pair;

public class Game extends Thread {

    final int SCORE_TO_WIN = 3;

    Player player1;
    Player player2;
    Long date;
    Long warmupTime;

    public int moveCount = 0;

    public int scorePlayer1 = 0;
    public int scorePlayer2 = 0;

    PublishSubject gameCallback;

    String player1Move;
    String player2Move;

    List<Move> moveList;

    Disposable gameTimer;

    Map<String, String> env = System.getenv();
    int MOVE_TIMEOUT = env.containsKey("RPSLS_MOVE_TIMEOUT") ?
            Integer.parseInt(env.get("RPSLS_MOVE_TIMEOUT"))
            : 10;


    Game(Player player1, Player player2, PublishSubject gameCallback) {
        this.player1 = player1;
        this.player2 = player2;
        this.date = System.currentTimeMillis();
        this.gameCallback = gameCallback;
        this.moveList = new ArrayList<Move>();
        player1.incommingMessage
            .doOnError(e -> e.printStackTrace())
            .subscribe(message -> {
            System.out.println("InGame1: " + message.getKey());
            switch (message.getKey()) {
                case "MOVE":
                    player1Move = message.getValue();
                    break;
            }
        });
        player2.incommingMessage
            .doOnError(e -> e.printStackTrace())
            .subscribe(message -> {
            System.out.println("InGame1: " + message.getKey());
            switch (message.getKey()) {
                case "MOVE":
                    player2Move = message.getValue();
                    break;
            }
        });
        this.player1.sendAuthReq();
        this.player2.sendAuthReq();
        Observable.timer(5, TimeUnit.SECONDS).subscribe(time -> {
            this.run();
            this.warmupTime = System.currentTimeMillis();
        });
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

            Observable.interval(MOVE_TIMEOUT, TimeUnit.SECONDS)
                    .doOnError(e -> e.printStackTrace())
                    .doOnSubscribe(e -> {
                        String firstMoveReq = socketHelper.padRight("MOVE_REQ") + "\n";
                        player1.socket.send(firstMoveReq);
                        player2.socket.send(firstMoveReq);
                    })
                    .takeUntil(i -> !!gameOver())
                    .doOnComplete(onGameEnd())
                    .doOnNext(time -> {
                        String statusMessage;
                        if (!legalMove(player2Move) && !legalMove(player1Move)) {
                            updateGame(0);
                            statusMessage = statusMessage(0);
                        } else if (!legalMove(player1Move)) {
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
                        Move move = new Move(System.currentTimeMillis(), player1Move, player2Move);
                        moveList.add(move);
                        player1Move = null;
                        player2Move = null;
                        player1.socket.send(statusMessage);
                        player2.socket.send(statusMessage);
                        if (!gameOver()) {
                            String moveReq = socketHelper.padRight("MOVE_REQ") + "\n";
                            player1.socket.send(moveReq);
                            player2.socket.send(moveReq);
                        }
                    }).subscribe();
        } catch (Exception e) {
            System.out.println("Player died: " + e);
        }
    }

    boolean gameOver() {
        return scorePlayer1 >= SCORE_TO_WIN | scorePlayer2 >= SCORE_TO_WIN;
    }

    Action onGameEnd() {
        return new Action() {
            @Override
            public void run() throws Exception {
                String report;
                if (scorePlayer1 > scorePlayer2) {
                    player1.socket.send(winMessage(1));
                    player2.socket.send(loseMessage(1));
                    report = buildReport(1);
                }
                else if (scorePlayer2 > scorePlayer1) {
                    player2.socket.send(winMessage(2));
                    player1.socket.send(loseMessage(2));
                    report = buildReport(2);
                } else {
                    report = buildReport(0);
                }
                player1.socket.send(report);
                player2.socket.send(report);
                gameCallback.onNext(new Pair(player1, player2));
            }
        };
    }

    private String moveReport() {
        String[] moves = moveList.stream().map(move -> {
            Object[] params = new Object[]{ move.time.toString(), move.player1move, move.player2move};
            return MessageFormat.format("<move time=\"{0}\" player1=\"{1}\" player2=\"{2}\" />", params);
        }).toArray(String[]::new);
        return String.join("", moves);
    }

    private String warmupReport() {
        Object[] params = new Object[]{ warmupTime.toString(), player1.login, player2.login };
        return MessageFormat.format("<warmup time=\"{0}\"><check for=\"{1}\"/><check for=\"{2}\"/></warmup>", params);
    }

    private String buildReport(int winner) {
        String winnerLogin;
        switch(winner) {
            case 1:
                winnerLogin = player1.login;
                break;
            case 2:
                winnerLogin = player2.login;
                break;
            default:
                winnerLogin = "";
                break;
        }
        Object[] params = new Object[]{ player1.login, player2.login, winnerLogin, date.toString() };
        String gameReport = MessageFormat.format("<game player1=\"{0}\" player2=\"{1}\" winner=\"{2}\" time=\"{3}\">", params);
        return socketHelper.padRight("REPORT") + gameReport + warmupReport() + moveReport() + "</game>\n";
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

    private String winMessage(int winner) {
        if (winner == 1)
            return socketHelper.padRight("WIN") + player1.login + ";" + player2.login + ";" + scorePlayer2 + "\n";
        else if (winner == 2)
            return socketHelper.padRight("WIN") + player2.login + ";" + player1.login + ";" + scorePlayer1 + "\n";
        return "";
    }

    private String loseMessage(int winner) {
        if (winner == 2)
            return socketHelper.padRight("LOSE") + player2.login + ";" + player1.login + ";" + scorePlayer1 + "\n";
        else if (winner == 1)
            return socketHelper.padRight("LOSE") + player1.login + ";" + player2.login + ";" + scorePlayer2 + "\n";
        return "";
    }

}
