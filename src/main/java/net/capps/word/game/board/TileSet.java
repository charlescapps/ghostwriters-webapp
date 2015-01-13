package net.capps.word.game.board;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by charlescapps on 1/12/15.
 */
public class TileSet {
    public final Tile[][] tiles;
    public final int N;
    private final int TOTAL_TILES;

    public TileSet(int N) {
        Preconditions.checkArgument(N > 0, "The board size, N, must be positive.");
        this.N = N;
        this.TOTAL_TILES = N*N;
        this.tiles = new Tile[N][N];
    }

    public void load(InputStreamReader reader) throws IOException {
        final char[] input = new char[TOTAL_TILES];
        int totalCharsRead = 0;
        while (totalCharsRead < TOTAL_TILES) {
            int numRead = reader.read(input, totalCharsRead, TOTAL_TILES - totalCharsRead);
            if (numRead < 0) {
                throw new RuntimeException("End of input reached before N*N tiles were read. Actual chars read = " + totalCharsRead);
            }
            totalCharsRead += numRead;
        }
        for (int i = 0; i < input.length; i++) {
            int row = i / N;
            int col = i % N;
            tiles[row][col] = Tile.valueOf(input[i]);
        }
    }
}
