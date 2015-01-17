package net.capps.word.game.board;

import com.google.common.base.Preconditions;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Move;
import net.capps.word.game.common.Pos;
import net.capps.word.game.tile.Tile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                tiles[r][c] = Tile.absent();
            }
        }
    }

    public void load(InputStreamReader reader) throws IOException, InvalidBoardException {
        final char[] input = new char[1024];
        StringBuilder sb = new StringBuilder();
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
            this.tiles[row][col] = Tile.of(compactTiles.charAt(i));
        }
    }

    public void placeMove(Move move) {
        Dir dir = move.getDir();
        Pos start = move.getStart();
        List<Tile> tilesPlayed = move.getTilesPlayed();
        checkTilesAreValid(tilesPlayed);

        switch (dir) {
            case E:
                final int r = start.r;
                for (int i = 0; i < tilesPlayed.size(); i++) {
                    tiles[r][start.c + i] = tilesPlayed.get(i);
                }
                break;
            case S:
                final int c = start.c;
                for (int i = 0; i < tilesPlayed.size(); i++) {
                    tiles[start.r + i][c] = tilesPlayed.get(i);
                }
                break;
        }
    }

    public void checkTilesAreValid(List<Tile> tilesPlayed) {
        for (Tile tile: tilesPlayed) {
            if (!tile.isPlayable()) {
                throw new IllegalArgumentException("Cannot play absent tiles or wild tiles without a character on the board!");
            }
        }
    }
}
