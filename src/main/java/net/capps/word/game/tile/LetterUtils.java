package net.capps.word.game.tile;

/**
 * Created by charlescapps on 1/21/15.
 */
public class LetterUtils {
    /**
     * Performance-paranoid method to check if a char is an uppercase, English letter.
     *
     * The Java method Character.isUpperCase includes too many letters as "uppercase".
     */
    public static boolean isUppercase(char c) {
        return 'A' <= c && c <= 'Z';
    }

    /**
     * Performance-paranoid method to check if a char is a lowercase, English letter.
     */
    public static boolean isLowercase(char c) {
        return 'a' <= c && c <= 'z';
    }
}
