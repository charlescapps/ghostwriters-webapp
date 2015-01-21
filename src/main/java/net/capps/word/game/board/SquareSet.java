package net.capps.word.game.board;

import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.PosIterator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Created by charlescapps on 1/12/15.
 */
public class SquareSet implements Iterable<Pos> {
    public final int N;
    public final Square[][] squares;
    private final int TOTAL_SQUARES;

    public SquareSet(int N) {
        this.N = N;
        this.TOTAL_SQUARES = N*N;
        this.squares = new Square[N][N];
    }

    public SquareSet(BoardSize boardSize) {
        this(boardSize.getN());
    }

    public Square get(Pos p) {
        if (!p.isValid() || p.N != N) {
            throw new IllegalArgumentException("Must provide a valid position!");
        }
        return squares[p.r][p.c];
    }

    public void set(Pos p, Square s) {
        if (!p.isValid() || p.N != N) {
            throw new IllegalArgumentException("Must provide a valid position!");
        }
        squares[p.r][p.c] = s;
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

    @Override
    public Iterator<Pos> iterator() {
        return new PosIterator(N);
    }


}
