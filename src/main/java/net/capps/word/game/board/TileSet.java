package net.capps.word.game.board;

import com.google.common.base.Preconditions;
import net.capps.word.exceptions.InvalidBoardException;

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
        Preconditions.checkArgument(N > 6, "The board size, N, must be at least 7.");
        this.N = N;
        this.TOTAL_TILES = N * N;
        this.tiles = new Tile[N][N];
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

        if (compactTiles.length() != TOTAL_TILES) {
            final String msg = String.format(
                    "Invalid Tile config string. Needed NxN tiles (%d) but found %d tiles.\nInvalid config: %s",
                    TOTAL_TILES, compactTiles.length(), tileConfig);
            throw new InvalidBoardException(msg);
        }


        for (int i = 0; i < compactTiles.length(); i++) {
            int row = i / N;
            int col = i % N;
            this.tiles[row][col] = Tile.valueOf(compactTiles.charAt(i));
        }
    }
}
