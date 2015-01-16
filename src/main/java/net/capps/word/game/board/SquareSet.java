package net.capps.word.game.board;

import net.capps.word.exceptions.InvalidBoardException;

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

    public void load(InputStreamReader reader) throws IOException, InvalidBoardException {
        final char[] input = new char[1024];
        StringBuffer sb = new StringBuffer();
        int numRead;
        do {
            numRead = reader.read(input);
            sb.append(input, 0, numRead);
        } while (numRead != -1);

        String tileConfig = sb.toString();

        // Remove whitespace, which can be added to tile configuration files
        String compactTiles = tileConfig.replaceAll("\\s+", "");

        if (compactTiles.length() != TOTAL_SQUARES) {
            final String msg = String.format(
                    "Invalid Squares config string. Needed NxN tiles (%d) but found %d tiles.\nInvalid config: %s",
                    TOTAL_SQUARES, compactTiles.length(), tileConfig);
            throw new InvalidBoardException(msg);
        }


        for (int i = 0; i < compactTiles.length(); i++) {
            int row = i / N;
            int col = i % N;
            this.squares[row][col] = Square.valueOf(compactTiles.charAt(i));
        }
    }
}
