import javafx.util.Pair;

public class socketHelper {

    public static String padRight(String s) {
        return s + "----------------".substring(s.length());
    }

    public static Pair<String, String> messageBroker(String message) {
        String command = message.substring(0, 15).substring( 0, message.indexOf( "-" ) );
        String parameters = message.substring(16, message.indexOf( "\n" ));
        return new Pair<String, String>(command, parameters);
    }
}
