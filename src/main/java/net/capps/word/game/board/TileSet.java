package net.capps.word.game.board;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.PosIterator;
import net.capps.word.game.dict.Dictionaries;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.LetterUtils;
import net.capps.word.game.tile.RackTile;
import net.capps.word.game.tile.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/12/15.
 */
public class TileSet implements Iterable<Pos> {
    private static final Logger LOG = LoggerFactory.getLogger(TileSet.class);
    private static final DictionarySet DICTIONARY_SET = Dictionaries.getAllWordsSet();

    public final Tile[][] tiles;
    public final int N;

    public TileSet(int N) {
        Preconditions.checkArgument(N >= 5, "The board size, N, must be at least 5.");
        this.N = N;
        this.tiles = new Tile[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                tiles[r][c] = Tile.absentTile();
            }
        }
    }

    public Tile get(Pos p) {
        return tiles[p.r][p.c];
    }

    public void set(Pos p, Tile tile) {
        tiles[p.r][p.c] = tile;
    }

    public boolean isValid(Pos p) {
        return p.r >= 0 && p.r < N && p.c >= 0 && p.c < N;
    }

    public List<Pos> getAllStartTilePositions() {
        List<Pos> posList = Lists.newArrayList();
        for (Pos p: this) {
            Tile tile = get(p);
            if (!tile.isAbsent() && tile.isStartTile()) {
                posList.add(p);
            }
        }
        return posList;
    }

    public boolean areAllTilesPlayed() {
        for (Pos p: this) {
            Tile tile = get(p);
            if (tile.isAbsent()) {
                continue;
            }
            if (tile.isStartTile()) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        for (Pos p: this) {
            Tile tile = get(p);
            if (!tile.isAbsent()) {
                return false;
            }
        }
        return true;
    }

    public char getLetterAt(Pos p) {
        Tile tile = tiles[p.r][p.c];
        return tile.getLetter();
    }

    public String getWord(Pos start, Pos end) {

        int diff = end.minus(start);
        Dir dir = start.getDirTo(end);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= diff; i++) {
            Pos p = start.go(dir, i);
            sb.append(getLetterAt(p));
        }
        return sb.toString();
    }

    public String getWordWithMissingChar(Pos start, Pos end, Pos middle, char middleChar) {
        int diff = end.minus(start);
        Dir dir = start.getDirTo(end);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= diff; i++) {
            Pos p = start.go(dir, i);
            if (p.equals(middle)) {
                sb.append(middleChar);
            } else {
                sb.append(getLetterAt(p));
            }
        }
        return sb.toString();
    }

    public void load(Reader reader) throws IOException, InvalidBoardException {
        final char[] input = new char[1024];
        StringBuilder sb = new StringBuilder();
        int numRead;
        do {
            numRead = reader.read(input);
            if (numRead > 0) {
                sb.append(input, 0, numRead);
            }
        } while (numRead != -1);

        String tileConfig = sb.toString();

        boolean wild = false;
        int numTilesRead = 0;
        for (int i = 0; i < tileConfig.length(); i++) {
            char c = tileConfig.charAt(i);
            if (Character.isWhitespace(c)) {
                continue; // ignore whitespace.
            }
            // If the char is alphabetic, add a Tile to the matrix.
            if (LetterUtils.isLowercase(c) || LetterUtils.isUppercase(c) || c == Tile.ABSENT_TILE) {
                int row = numTilesRead / N;
                int col = numTilesRead % N;
                tiles[row][col] = Tile.fromSerializedForm(c, wild);
                ++numTilesRead;
            }
            // If a '*' is encountered, the subsequent Tile is marked as a Wildcard
            wild = c == Tile.WILD_TILE;
        }
        if (numTilesRead != N * N) {
            throw new IllegalStateException(
                    format("The input tileconfig didn't have N*N = %d characters:\n%s", N * N, tileConfig));
        }
    }

    public void playWordMove(Move move, SquareSet squareSet) {
        Preconditions.checkArgument(move.getMoveType() == MoveType.PLAY_WORD, "Move type must be Play Word.");
        Dir dir = move.getDir();
        Pos start = move.getStart();
        List<RackTile> tilesPlayed = move.getTiles();
        String word = move.getLetters();

        int rackIndex = 0;
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            Pos p = start.go(dir, i);
            Tile existing = get(p);
            if (existing.isAbsent()) {
                RackTile rackTile = tilesPlayed.get(rackIndex++);
                set(p, rackTile.toTile(letter));
            } else {
                if (existing.getLetter() != letter) {
                    throw new IllegalStateException("Attempting to place invalid move: " + move);
                }
                set(p, Tile.playedTile(letter)); // Any tile that is part of the played word is now "set in stone"
            }
        }
        if (rackIndex != tilesPlayed.size()) {
            throw new IllegalStateException("All the played tiles should have been used. Invalid move: " + move);
        }

    }

    public void playGrabTilesMove(Move move) {
        Preconditions.checkArgument(move.getMoveType() == MoveType.GRAB_TILES, "Move type must be Grab Tiles.");
        Dir dir = move.getDir();
        Pos start = move.getStart();
        List<RackTile> tilesGrabbed = move.getTiles();
        String word = move.getLetters();

        for (int i = 0; i < word.length(); i++) {
            Pos p = start.go(dir, i);
            Tile existing = get(p);
            if (existing.isAbsent()) {
                throw new IllegalStateException("Cannot grab an empty tile!");
            }
            RackTile rackTile = existing.toRackTile();
            if (!rackTile.equals(tilesGrabbed.get(i))) {
                throw new IllegalStateException(
                        format("For grab tiles move, at index %d, board has %s but tile grabbed is %s. Move: %s",
                                i, existing.toString(), tilesGrabbed.get(i).toString(), move.toString()));
            }
            // Remove the tile being grabbed.
            set(p, Tile.absentTile());
        }

    }

    public void placeWord(Placement placement) {
        final Dir dir = placement.getDir();
        final Pos start = placement.getStart();
        final String word = placement.getWord();

        switch (dir) {
            case E:
                final int r = start.r;
                for (int i = 0; i < word.length(); i++) {
                    tiles[r][start.c + i] = Tile.startTile(word.charAt(i));
                }
                break;
            case S:
                final int c = start.c;
                for (int i = 0; i < word.length(); i++) {
                    tiles[start.r + i][c] = Tile.startTile(word.charAt(i));
                }
                break;
        }
    }

    public Optional<String> isValidPlayWordMove(Move move) {
        // Check if the tiles played from the player's Rack match what's on the board
        Optional<String> errorOpt = lettersMatchTilesPlayed(move.getStart(), move.getDir(), move.getLetters(), move.getTiles());
        if (errorOpt.isPresent()) {
            return errorOpt;
        }

        Placement placement = move.getPlacement();
        errorOpt = getPlacementError(placement);
        if (errorOpt.isPresent()) {
            return errorOpt;
        }

        return Optional.absent();
    }

    public Optional<String> isValidGrabTilesMove(Move move) {
        // Check if the move starts in a valid place.
        if (!isOccupied(move.getStart())) {
            return Optional.of("Grab Tiles move must start on an occupied tile");
        }

        // Check if the tiles being added to the Rack match what's taken from the board.
        Optional<String> errorOpt = lettersMatchTilesGrabbed(move.getStart(), move.getDir(), move.getLetters(), move.getTiles());
        if (errorOpt.isPresent()) {
            return errorOpt;
        }

        return Optional.absent();
    }

    private Optional<String> doesMoveStartInValidPosition(Pos start, Dir dir) {
        if (!isValid(start)) {
            return Optional.of("Start position is off the board.");
        }

        Pos previous = start.go(dir.negate());
        if (isOccupied(previous)) {
            return Optional.of("A move can't start in the middle of a word on the board!");
        }
        return Optional.absent();
    }

    private Optional<String> lettersMatchTilesPlayed(Pos start, Dir dir, String letters, List<RackTile> tiles) {
        int rackIndex = 0;
        for (int i = 0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            Pos p = start.go(dir, i);
            Tile tile = get(p);
            if (tile.isAbsent()) {
                RackTile toPlay = tiles.get(rackIndex++);
                // The next tile on the rack must match the character of the word being played, or be wild.
                if (!toPlay.isWild() && toPlay.getLetter() != c) {
                    return Optional.of("The next character of the word being played must match the tile being played.");
                }
            } else {
                // The tile on the board must match the next character of the word being played
                if (c != tile.getLetter()) {
                    return Optional.of("The tile on the board must match the next character of the word played.");
                }
            }
        }

        if (rackIndex != tiles.size()) {
            return Optional.of("All tiles being played must be consumed.");
        }

        return Optional.absent();
    }

    private Optional<String> lettersMatchTilesGrabbed(Pos start, Dir dir, String letters, List<RackTile> tiles) {
        if (tiles.size() != letters.length()) {
            return Optional.of(format("The letters \"%s\" don't match the number of tiles grabbed, %d", letters, tiles.size()));
        }
        for (int i = 0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            Pos p = start.go(dir, i);
            Tile tile = get(p);
            RackTile rackTile = tiles.get(i);
            // Can't grab an absent tile
            if (tile.isAbsent()) {
                return Optional.of("Cannot grab an empty space!");
            }
            // Can't grab a tile that was played by a player
            if (!tile.isStartTile()) {
                return Optional.of("Cannot grab a played tile!");
            }
            // A grabbed wildcard tile becomes a wildcard tile in the player's rack
            if (tile.isWild() != rackTile.isWild()) {
                return Optional.of(format("Grabbed tile \"%s\" on board, but rack tile is \"%s\"", tile.toString(), rackTile.toString()));
            }
            // The tile on the board must match the next character of the word being grabbed
            if (c != tile.getLetter()) {
                return Optional.of("The tile on the board must match the next character of the word grabbed.");
            }
            // Letters must match
            if (!tile.isWild() && tile.getLetter() != rackTile.getLetter()) {
                return Optional.of(format("The next tile, \"%s\", doesn't match the rack tile, \"%s\"", tile.toString(), rackTile.toString()));
            }

        }

        return Optional.absent();
    }

    public Optional<String> getPlacementError(Placement placement) {
        final String word = placement.getWord();
        final Dir dir = placement.getDir();

        // Must be a valid dictionary word
        if (!DICTIONARY_SET.contains(word)) {
            return Optional.of(format("\"%s\" is not a valid dictionary word.", word));
        }

        // Can only play EAST or SOUTH
        if (!dir.isValidPlayDir()) {
            return Optional.of(format("%s is not a valid direction to play. Can only play South or East.", dir));
        }

        Optional<String> errorOpt = doesMoveStartInValidPosition(placement.getStart(), placement.getDir());
        if (errorOpt.isPresent()) {
            return errorOpt;
        }

        // Must be a valid play in the primary direction
        errorOpt = isValidPlacementInPrimaryDir(placement);
        if (errorOpt.isPresent()) {
            return errorOpt;
        }

        // Must be a valid play with words formed perpendicularly
        return getErrorForPerpendicularPlacement(placement);
    }

    private Optional<String> isValidPlacementInPrimaryDir(Placement placement) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
        final Dir dir = placement.getDir();

        for (int i = 0; i < word.length(); i++) {
            Pos p = start.go(dir, i);

            // Placement can't go off end of board
            if (!isValid(p)) {
                return Optional.of("Play would go off the board!");
            }

            // Letter must match occupied tiles
            if (isOccupied(p)) {
                if (getLetterAt(p) != word.charAt(i)) {
                    return Optional.of("Word played must match existing tiles!");
                }
            }
        }

        Pos afterWordPos = start.go(dir, word.length());
        if (isOccupied(afterWordPos)) {
            return Optional.of("Letters given to play must include all consecutive letters on the board from start position.");
        }
        return Optional.absent();
    }

    private Optional<String> getErrorForPerpendicularPlacement(final Placement placement) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
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
                if (!DICTIONARY_SET.contains(perpWord.get())) {
                    return Optional.of(format("Sorry, \"%s\" isn't in our dictionary.", perpWord.get()));
                }
            }
        }
        return Optional.absent();
    }

    private Optional<String> getPerpWordForAttemptedPlacement(final Pos pos, char missingChar, Dir dir) {
        // Precondition: pos isn't an occupied tile.
        switch (dir) {
            case E:
            case W:
                Pos start = getEndOfOccupied(pos.n(), Dir.N);
                Pos end = getEndOfOccupied(pos.s(), Dir.S);
                if (start.equals(end)) {
                    return Optional.absent();
                }
                return Optional.of(getWordWithMissingChar(start, end, pos, missingChar));

            case S:
            case N:
                start = getEndOfOccupied(pos.w(), Dir.W);
                end = getEndOfOccupied(pos.e(), Dir.E);
                if (start.equals(end)) {
                    return Optional.absent();
                }
                return Optional.of(getWordWithMissingChar(start, end, pos, missingChar));
        }
        return Optional.absent();
    }

    public boolean isOccupiedOrAdjacentOccupied(Pos p) {
        if (!isValid(p)) {
            return false;
        }
        return isOccupied(p) || isOccupied(p.s()) || isOccupied(p.e()) || isOccupied(p.n()) || isOccupied(p.w());
    }

    public boolean isOccupied(Pos pos) {
        if (!isValid(pos)) {
            return false;
        }
        return !tiles[pos.r][pos.c].isAbsent();
    }

    /**
     * Starting at position "start", going in direction "dir" at most "maxLen" distance,
     * find the first tile that is occupied or an adjacent tile in any direction is occupied.
     */
    public Optional<Pos> getFirstOccupiedOrAdjacent(Pos start, Dir dir, int maxLen) {

        for (int i = 0; i < maxLen; i++) {
            Pos p = start.go(dir, i);
            if (!isValid(p)) {
                return Optional.absent();
            }
            if (isOccupiedOrAdjacentOccupied(p)) {
                return Optional.of(p);
            }
        }
        return Optional.absent();
    }

    public Pos getEndOfOccupied(Pos start, Dir dir) {
        Pos p = start;
        while (isOccupied(p)) {
            p = p.go(dir);
        }
        return p.go(dir, -1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                sb.append(tiles[r][c]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                sb.append(tiles[r][c]);
            }
        }
        return sb.toString();
    }

    @Override
    public Iterator<Pos> iterator() {
        return new PosIterator(N);
    }

}
