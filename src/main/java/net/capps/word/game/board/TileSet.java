package net.capps.word.game.board;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.sun.javafx.tools.packager.Log;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Move;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.tile.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by charlescapps on 1/12/15.
 */
public class TileSet {
    private static final Logger LOG = LoggerFactory.getLogger(TileSet.class);

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

    public char getLetterAt(Pos p) {
        if (!p.isValid()) {
            throw new IllegalArgumentException("Position is invalid: " + p);
        }
        Tile tile = tiles[p.r][p.c];
        if (!tile.isLetterTile()) {
            LOG.info("Tiles:\n{}", this);
            LOG.info("Pos to get letter: {}", p);
            throw new IllegalArgumentException("Cannot get letter from tile: " + tile);
        }
        return tile.getLetter();
    }

    public String getWord(Pos start, Pos end) {
        if (!start.isValid() || !end.isValid()) {
            throw new IllegalArgumentException("Cannot find word for invalid positions");
        }

        int diff = end.minus(start);
        Dir dir = start.getDirTo(end);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= diff; i++) {
            Pos p = start.go(dir, i);
            sb.append(getLetterAt(p));
        }
        return sb.toString();
    }

    public String getPerpWord(Pos start, Pos end, Pos middle, char c) {
        if (!start.isValid() || !end.isValid()) {
            throw new IllegalArgumentException("Cannot find word for invalid positions");
        }

        int diff = end.minus(start);
        Dir dir = start.getDirTo(end);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= diff; i++) {
            Pos p = start.go(dir, i);
            if (p.equals(middle)) {
                sb.append(c);
            } else {
                sb.append(getLetterAt(p));
            }
        }
        return sb.toString();
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

    public void placeWord(Placement placement) {
        final Dir dir = placement.getDir();
        final Pos start = placement.getStartPos();
        final String word = placement.getWord();

        switch (dir) {
            case E:
                final int r = start.r;
                for (int i = 0; i < word.length(); i++) {
                    tiles[r][start.c + i] = Tile.of(word.charAt(i));
                }
                break;
            case S:
                final int c = start.c;
                for (int i = 0; i < word.length(); i++) {
                    tiles[start.r + i][c] = Tile.of(word.charAt(i));
                }
                break;
        }
    }

    public boolean isValidPlacement(Placement placement) {
        final String word = placement.getWord();
        final Dir dir = placement.getDir();

        // Must be a valid dictionary word
        if (!DictionarySet.getInstance().contains(word)) {
            return false;
        }

        // Can only play EAST or SOUTH
        if (!dir.isValidPlayDir()) {
            return false;
        }

        // Must be a valid play in the primary direction
        if (!isValidPlacementInPrimaryDir(placement)) {
            return false;
        }

        // Must be a valid play with words formed perpendicularly
        if (!isValidPlacementInPerpendicular(placement)) {
            return false;
        }

        return true;
    }

    private boolean isValidPlacementInPrimaryDir(Placement placement) {
        final String word = placement.getWord();
        final Pos start = placement.getStartPos();
        final Dir dir = placement.getDir();

        Pos previous = start.go(dir.negate());
        if (isOccupied(previous)) {
            LOG.error("Board:\n{}", this);
            LOG.error("Placing word {}, start {}, dir {}", word, start, dir);
            throw new IllegalStateException("A placement must include the start of the word!");
        }

        for (int i = 0; i < word.length(); i++) {
            Pos p = start.go(dir, i);

            // Placement can't go off end of board
            if (!p.isValid()) {
                return false;
            }

            // Letter must match occupied tiles
            if (isOccupied(p)) {
                if (getLetterAt(p) != word.charAt(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidPlacementInPerpendicular(Placement placement) {
        final String word = placement.getWord();
        final Pos start = placement.getStartPos();
        final Dir dir = placement.getDir();

        for (int i = 0; i < word.length(); i++) {
            Pos p = start.go(dir, i);
            // Don't need to check already occupied squares.
            if (isOccupied(p)) {
                continue;
            }
            char c = word.charAt(i);
            Optional<String> perpWord = getPerpWordForAttemptedPlacement(p, c, dir);
            if (perpWord.isPresent()) {
                if (!DictionarySet.getInstance().contains(perpWord.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    private Optional<String> getPerpWordForAttemptedPlacement(final Pos pos, char c, Dir dir) {
        if (isOccupied(pos)) {
            throw new IllegalArgumentException("Cannot get perpendicular word for occupied space.");
        }
        switch (dir) {
            case E:
            case W:
                Pos n = pos.n();
                Pos s = pos.s();
                if (!isOccupied(n) && !isOccupied(s)) {
                    return Optional.absent();
                }
                Pos start = pos;
                if (isOccupied(n)) {
                    start = getEndOfOccupied(n, Dir.N);
                }
                Pos end = pos;
                if (isOccupied(s)) {
                    end = getEndOfOccupied(s, Dir.S);
                }
                return Optional.of(getPerpWord(start, end, pos, c));

            case S:
            case N:
                Pos e = pos.e();
                Pos w = pos.w();
                if (!isOccupied(e) && !isOccupied(w)) {
                    return Optional.absent();
                }
                start = pos;
                if (isOccupied(w)) {
                    start = getEndOfOccupied(w, Dir.W);
                }
                end = pos;
                if (isOccupied(e)) {
                    end = getEndOfOccupied(e, Dir.E);
                }
                return Optional.of(getPerpWord(start, end, pos, c));
        }
        return Optional.absent();
    }

    public boolean isOccupiedOrAdjacentOccupied(Pos pos) {
        if (!pos.isValid()) {
            return false;
        }
        return isOccupied(pos) || isOccupied(pos.s()) || isOccupied(pos.e()) || isOccupied(pos.n()) || isOccupied(pos.w());
    }

    public boolean isOccupied(Pos pos) {
        if (!pos.isValid()) {
            return false;
        }
        return !tiles[pos.r][pos.c].isAbsent();
    }

    public void checkTilesAreValid(List<Tile> tilesPlayed) {
        for (Tile tile: tilesPlayed) {
            if (!tile.isLetterTile()) {
                throw new IllegalArgumentException("Cannot play absent tiles or wild tiles without a character on the board!");
            }
        }
    }

    public Optional<Pos> getFirstOccupiedOrAdjacent(Pos start, Dir dir, int maxLen) {

        for (int i = 0; i < maxLen; i++) {
            Pos pos = start.go(dir, i);
            if (!pos.isValid()) {
                return Optional.absent();
            }
            if (isOccupiedOrAdjacentOccupied(pos)) {
                return Optional.of(pos);
            }
        }
        return Optional.absent();
    }

    public Pos getEndOfOccupied(Pos start, Dir dir) {
        Pos p = start;
        while (p.isValid() && isOccupied(p)) {
            p = p.go(dir);
        }
        return p.go(dir, -1);
    }

    public Pos getEndOfOccupiedFromOccupiedOrAdj(Pos start, Dir dir) {
        // Case 1: start is occupied.
        if (isOccupied(start)) {
            Pos pos = start;

            while (pos.isValid() && isOccupied(pos)) {
                pos = pos.go(dir);
            }
            return pos.go(dir, -1);
        }

        // Case 2: start is unoccupied
        Pos pos = start.go(dir);
        if (isOccupied(pos)) {
          while (pos.isValid() && isOccupied(pos)) {
              pos = pos.go(dir);
          }
          return pos.go(dir, -1);
        } else {
            return start;
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                sb.append(tiles[r][c]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
