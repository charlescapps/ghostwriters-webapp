package net.capps.word.game.board;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by charlescapps on 1/12/15.
 */
public class SquareSet {
    public final int N;
    public final Square[][] squares;
    private final int TOTAL_SQUARES;

    public SquareSet(int N) {
        this.N = N;
        this.TOTAL_SQUARES = N*N;
        this.squares = new Square[N][N];
    }

    public void load(InputStreamReader reader) throws IOException {
        final char[] input = new char[TOTAL_SQUARES];
        int totalCharsRead = 0;
        while (totalCharsRead < TOTAL_SQUARES) {
            int numRead = reader.read(input, totalCharsRead, TOTAL_SQUARES - totalCharsRead);
            if (numRead < 0) {
                throw new RuntimeException("End of input reached before N*N tiles were read. Actual chars read = " + totalCharsRead);
            }
            totalCharsRead += numRead;
        }
        for (int i = 0; i < input.length; i++) {
            int row = i / N;
            int col = i % N;
            squares[row][col] = Square.valueOf(input[i]);
        }
    }
}
