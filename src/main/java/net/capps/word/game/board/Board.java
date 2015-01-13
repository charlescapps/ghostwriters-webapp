package net.capps.word.game.board;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by charlescapps on 1/12/15.
 */
public class Board {
    private final int N;
    private final SquareSet squareSet;
    private final TileSet tileSet;

    public Board(int N) {
        this.N = N;
        squareSet = new SquareSet(N);
        tileSet = new TileSet(N);
    }

    public void load(InputStreamReader squareReader, InputStreamReader tileReader) throws IOException {
        squareSet.load(squareReader);
        tileSet.load(tileReader);
    }


}
