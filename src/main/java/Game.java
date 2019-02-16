import javax.lang.model.type.ArrayType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    Player player1;
    Player player2;

    public int moveCount = 0;

    public int scorePlayer1 = 0;
    public int scorePlayer2 = 0;

    Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
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


    public synchronized boolean legalMove(Player player, String gesture) {
        List<String> validGesture = List.of("rock", "paper", "scissor", "lizard", "spock");
        return validGesture.contains(gesture);
    }

}
