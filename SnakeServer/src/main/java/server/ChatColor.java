package server;

public class ChatColor {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static String getColor(int i){
        if(i < 7){
            if (i%7 == 0) return RED;
            else if (i%7 == 1) return GREEN;
            else if (i%7 == 2) return YELLOW;
            else if (i%7 == 3) return BLUE;
            else if (i%7 == 4) return PURPLE;
            else if (i%7 == 5) return CYAN;
            else if (i%7 == 6) return WHITE;
        }
        return RESET;
    }
}
