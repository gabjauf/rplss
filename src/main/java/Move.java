import java.util.Date;

public class Move {
    Long time;
    String player1move;
    String player2move;

    Move(Long time, String player1move, String player2move) {
        this.time = time;
        this.player1move = player1move;
        this.player2move = player2move;
    }
}
