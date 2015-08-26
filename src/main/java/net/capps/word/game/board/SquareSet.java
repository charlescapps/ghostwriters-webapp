package net.capps.word.game.board;

import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.MutPos;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.PosIterator;

import java.io.IOException;
import java.io.Reader;
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
        for (Pos p: this) {
            set(p, Square.NORMAL);
        }
    }

    public SquareSet(BoardSize boardSize) {
        this(boardSize.getN());
    }

    public Square get(Pos p) {
        return squares[p.r][p.c];
    }

    public Square get(MutPos mp) {
        return squares[mp.r][mp.c];
    }

    public void set(Pos p, Square s) {
        if (!isValid(p)) {
            throw new IllegalArgumentException("Must provide a valid position!");
        }
        squares[p.r][p.c] = s;
    }

    public boolean isValid(Pos p) {
        return p.r >= 0 && p.r < N && p.c >= 0 && p.c < N;
    }

    public void load(Reader reader) throws IOException, InvalidBoardException {
        final char[] input = new char[1024];
        StringBuffer sb = new StringBuffer();
        int numRead;
        do {
            numRead = reader.read(input);
            if (numRead > 0) {
                sb.append(input, 0, numRead);
            }
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

    public int getNumAdjacentBonuses(Pos p) {
        if (!isValid(p)) {
            return 0;
        }
        int numAdjs = 0;
        Pos n = p.n();
        Pos s = p.s();
        Pos e = p.e();
        Pos w = p.w();

        if (isValid(n) && get(n) != Square.NORMAL) {
            ++numAdjs;
        }
        if (isValid(s) && get(s) != Square.NORMAL) {
            ++numAdjs;
        }
        if (isValid(e) && get(e) != Square.NORMAL) {
            ++numAdjs;
        }
        if (isValid(w) && get(w) != Square.NORMAL) {
            ++numAdjs;
        }
        return numAdjs;
    }

    @Override
    public Iterator<Pos> iterator() {
        return new PosIterator(N);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                sb.append(squares[r][c].getCharRep()).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                sb.append(squares[r][c].getCharRep());
            }
        }
        return sb.toString();
    }

}
