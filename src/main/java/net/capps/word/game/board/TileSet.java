package net.capps.word.game.board;

import com.google.common.base.Preconditions;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Placement;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.PosIterator;
import net.capps.word.game.dict.Dictionaries;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.LetterUtils;
import net.capps.word.game.tile.RackTile;
import net.capps.word.game.tile.Tile;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.ErrorWordModel;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/12/15.
 */
public class TileSet implements Iterable<Pos> {
    private static final DictionarySet DICTIONARY_SET = Dictionaries.getEnglishDictSet();

    // ----------- Errors ----------
    private static final Optional<String> ERR_GRAB_TILES_ON_EMPTY = Optional.of("Grab tiles move must start on an occupied tile");
    private static final Optional<String> ERR_START_POS_OFF_BOARD = Optional.of("Start position is off the board.");
    private static final Optional<String> ERR_INVALID_MOVE_START = Optional.of("A move can't start in the middle of a word on the board!");
    private static final Optional<String> ERR_MUST_PLAY_ALL_TILES = Optional.of("All tiles being played must be used in the word formed.");

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
        List<Pos> posList = new ArrayList<>();
        for (Pos p: this) {
            Tile tile = get(p);
            if (tile.isStartTile()) {
                posList.add(p);
            }
        }
        return posList;
    }

    public List<Pos> getAllUnoccupiedPositions() {
        List<Pos> posList = new ArrayList<>();
        for (Pos p: this) {
            Tile tile = get(p);
            if (tile.isAbsent()) {
                posList.add(p);
            }
        }
        return posList;
    }

    public boolean areAllTilesPlayed() {
        for (Pos p: this) {
            Tile tile = get(p);
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

    public String getWordWithMissingChar(Pos start, Pos end, Pos middle, char middleChar) {
        final int wordLen = end.minus(start) + 1;
        final Dir dir = start.getDirTo(end);
        final char[] word = new char[wordLen];

        for (int i = 0; i < wordLen; ++i, start = start.go(dir)) {
            if (start.r == middle.r && start.c == middle.c) {
                word[i] = middleChar;
            } else {
                word[i] = tiles[start.r][start.c].getLetter();
            }
        }
        return new String(word);
    }

    public void load(Reader reader) throws IOException, InvalidBoardException {
        final char[] input = new char[256];
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

    public void playWordMove(Move move) {
        Preconditions.checkArgument(move.getMoveType() == MoveType.PLAY_WORD, "Move type must be Play Word.");
        Dir dir = move.getDir();
        Pos start = move.getStart();
        List<RackTile> tilesPlayed = move.getTiles();
        String word = move.getLetters();

        int rackIndex = 0;
        Pos p = start;
        for (int i = 0; i < word.length(); i++, p = p.go(dir)) {
            char letter = word.charAt(i);
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
        final Dir dir = move.getDir();
        final Pos start = move.getStart();
        final List<RackTile> tilesGrabbed = move.getTiles();
        final String word = move.getLetters();

        final int wordLen = word.length();
        Pos p = start;
        for (int i = 0; i < wordLen; ++i, p = p.go(dir)) {
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

    public Optional<ErrorModel> getPlayWordMoveError(Move move, SpecialDict specialDict) {
        // Check if the tiles played from the player's Rack match what's on the board
        Optional<String> errorOpt = lettersMatchTilesPlayed(move.getStart(), move.getDir(), move.getLetters(), move.getTiles());
        if (errorOpt.isPresent()) {
            return errorOpt.map(msg -> new ErrorModel(msg));
        }

        Placement placement = move.getPlacement();
        return getPlacementError(placement, specialDict);
    }

    public boolean isValidPlayWordMove(Move move, SpecialDict specialDict) {
        // Check if the tiles played from the player's Rack match what's on the board
        if (!doLettersMatchTilesPlayed(move.getStart(), move.getDir(), move.getLetters(), move.getTiles())) {
            return false;
        }

        Placement placement = move.getPlacement();
        return isValidPlacement(placement, specialDict);
    }

    public Optional<String> isValidGrabTilesMove(Move move) {
        // Check if the move starts in a valid place.
        if (!isOccupiedAndValid(move.getStart())) {
            return ERR_GRAB_TILES_ON_EMPTY;
        }

        // Check if the tiles being added to the Rack match what's taken from the board.
        Optional<String> errorOpt = lettersMatchTilesGrabbed(move.getStart(), move.getDir(), move.getLetters(), move.getTiles());
        if (errorOpt.isPresent()) {
            return errorOpt;
        }

        return Optional.empty();
    }

    private Optional<String> getInvalidPositionError(Pos start, Dir dir) {
        if (!isValid(start)) {
            return ERR_START_POS_OFF_BOARD;
        }

        Pos previous = start.go(dir.negate());
        if (isOccupiedAndValid(previous)) {
            return ERR_INVALID_MOVE_START;
        }
        return Optional.empty();
    }

    private boolean doesMoveStartInValidPosition(Pos start, Dir dir) {
        if (!isValid(start)) {
            return false;
        }

        Pos previous = start.go(dir.negate());
        if (isOccupiedAndValid(previous)) {
            return false;
        }
        return true;
    }

    private Optional<String> lettersMatchTilesPlayed(final Pos start, final Dir dir, final String letters, List<RackTile> tiles) {
        int rackIndex = 0;
        Pos p = start;
        for (int i = 0; i < letters.length(); i++, p = p.go(dir)) {
            char c = letters.charAt(i);
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
            return ERR_MUST_PLAY_ALL_TILES;
        }

        return Optional.empty();
    }

    private boolean doLettersMatchTilesPlayed(Pos start, Dir dir, String letters, List<RackTile> tiles) {
        int rackIndex = 0;
        Pos p = start;
        for (int i = 0; i < letters.length(); i++, p = p.go(dir)) {
            char c = letters.charAt(i);
            Tile tile = get(p);
            if (tile.isAbsent()) {
                RackTile toPlay = tiles.get(rackIndex++);
                // The next tile on the rack must match the character of the word being played, or be wild.
                if (!toPlay.isWild() && toPlay.getLetter() != c) {
                    return false;
                }
            } else {
                // The tile on the board must match the next character of the word being played
                if (c != tile.getLetter()) {
                    return false;
                }
            }
        }

        return rackIndex == tiles.size();
    }

    private Optional<String> lettersMatchTilesGrabbed(final Pos start, final Dir dir, final String letters, List<RackTile> tiles) {
        if (tiles.size() != letters.length()) {
            return Optional.of(format("The letters \"%s\" don't match the number of tiles grabbed, %d", letters, tiles.size()));
        }
        Pos p = start;
        for (int i = 0; i < letters.length(); i++, p = p.go(dir)) {
            char c = letters.charAt(i);
            Tile tile = get(p);
            RackTile rackTile = tiles.get(i);
            // Can't grab a tile that was played by a player
            if (!tile.isStartTile()) {
                return Optional.of("Can only grab letters that started on the board!");
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

        return Optional.empty();
    }

    public Optional<ErrorModel> getPlacementError(Placement placement, SpecialDict specialDict) {
        final String word = placement.getWord();

        Optional<String> errorOpt = getInvalidPositionError(placement.getStart(), placement.getDir());
        if (errorOpt.isPresent()) {
            return Optional.of(new ErrorModel(errorOpt.get()));
        }

        final Dir dir = placement.getDir();

        // Can only play EAST or SOUTH
        if (!dir.isValidPlayDir()) {
            return Optional.of(new ErrorModel("Can only play South or East."));
        }

        // Must be a valid dictionary word
        if (!isValidWord(word, specialDict)) {
            ErrorModel errorModel = new ErrorModel(format("\"%s\" is not in the dictionary for this game!", word));
            ErrorWordModel errorWordModel = new ErrorWordModel(placement.getStart().toPosModel(), placement.getDir());
            errorModel.setErrorWord(errorWordModel);
            return Optional.of(errorModel);
        }

        // Must be a valid play in the primary direction
        errorOpt = getErrorForPrimaryDir(placement);
        if (errorOpt.isPresent()) {
            return Optional.of(new ErrorModel(errorOpt.get()));
        }

        // Must be a valid play with words formed perpendicularly
        return getErrorForPerpendicularPlacement(placement, specialDict);
    }

    public boolean isValidPlacement(Placement placement, SpecialDict specialDict) {
        final String word = placement.getWord();

        // Must be a valid dictionary word
        if (!isValidWord(word, specialDict)) {
            return false;
        }

        final Dir dir = placement.getDir();

        // Can only play EAST or SOUTH
        if (!dir.isValidPlayDir()) {
            return false;
        }

        if (!doesMoveStartInValidPosition(placement.getStart(), placement.getDir())) {
            return false;
        }

        // Must be a valid play in the primary direction
        if (!isValidPlayInPrimaryDir(placement)) {
            return false;
        }

        // Must be a valid play with words formed perpendicularly
        return isValidPerpendicularPlacement(placement, specialDict);
    }

    private boolean isValidWord(String word, SpecialDict specialDict) {
        if (DICTIONARY_SET.contains(word)) {
            return true;
        }
        if (specialDict == null) {
            return false;
        }
        if (specialDict.getDictType().getDictionary().contains(word)) {
            return true;
        }
        return false;
    }

    private Optional<String> getErrorForPrimaryDir(Placement placement) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
        final Dir dir = placement.getDir();

        Pos p = start;
        for (int i = 0; i < word.length(); ++i, p = p.go(dir)) {

            // Placement can't go off end of board
            if (!isValid(p)) {
                return Optional.of("Play would go off the board!");
            }

            // Letter must match occupied tiles
            if (!tiles[p.r][p.c].isAbsent()) {
                if (getLetterAt(p) != word.charAt(i)) {
                    return Optional.of("Word played must match existing tiles!");
                }
            }
        }

        if (isOccupiedAndValid(p)) {
            return Optional.of("Letters given to play must include all consecutive letters on the board from start position.");
        }
        return Optional.empty();
    }

    private boolean isValidPlayInPrimaryDir(final Placement placement) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
        final Dir dir = placement.getDir();

        Pos p = start;
        for (int i = 0; i < word.length(); ++i, p = p.go(dir)) {
            // Placement can't go off end of board
            if (!isValid(p)) {
                return false;
            }

            // Letter must match occupied tiles
            if (!tiles[p.r][p.c].isAbsent()) {
                if (tiles[p.r][p.c].getLetter() != word.charAt(i)) {
                    return false;
                }
            }
        }

        return !isOccupiedAndValid(p);
    }

    private Optional<ErrorModel> getErrorForPerpendicularPlacement(final Placement placement, final SpecialDict specialDict) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
        final Dir dir = placement.getDir();

        Pos p = start;
        for (int i = 0; i < word.length(); ++i, p = p.go(dir)) {
            // Don't need to check already occupied squares.
            if (!tiles[p.r][p.c].isAbsent()) {
                continue;
            }
            char c = word.charAt(i);
            String perpWord = getPerpWordForAttemptedPlacement(p, c, dir);
            if (perpWord != null) {
                if (!isValidWord(perpWord, specialDict)) {
                    // Return an ErrorModel with the position of the invalid word included.
                    final String msg = "\"" + perpWord + "\" isn't in this game's dictionary.";
                    ErrorModel errorModel = new ErrorModel(msg);
                    Dir perpDir = dir.perp();
                    Pos errStart = getEndOfOccupied(p, perpDir.negate());
                    ErrorWordModel errorWordModel = new ErrorWordModel(errStart.toPosModel(), perpDir);
                    errorModel.setErrorWord(errorWordModel);
                    return Optional.of(errorModel);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * ASSUMPTION: We already checked the placement is valid in the primary direction.
     */
    public boolean isValidPerpendicularPlacement(final Placement placement, final SpecialDict specialDict) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
        final Dir dir = placement.getDir();

        final int wordLen = word.length();
        Pos p = start;
        for (int i = 0; i < wordLen; ++i, p = p.go(dir)) {
            // Don't need to check already occupied squares.
            if (!tiles[p.r][p.c].isAbsent()) {
                continue;
            }
            char c = word.charAt(i);
            String perpWord = getPerpWordForAttemptedPlacement(p, c, dir);
            if (perpWord != null) {
                if (!isValidWord(perpWord, specialDict)) {
                    return false;
                }
            }

        }
        return true;
    }

    /**
     * Input mutable position pos is not modified.
     * @return perpendicular word from the starting position when playing in the given direction.
     */
    private String getPerpWordForAttemptedPlacement(final Pos pos, char missingChar, Dir dir) {
        // Precondition: pos isn't an occupied tile.
        Pos start, end;
        switch (dir) {
            case E:
                start = getEndOfOccupiedN(pos);
                end = getEndOfOccupiedS(pos);
                break;
            case S:
                start = getEndOfOccupiedW(pos);
                end = getEndOfOccupiedE(pos);
                break;
            default:
                throw new IllegalStateException("Invalid direction for word placement: " + dir);
        }
        if (start.r == end.r && start.c == end.c) {
            return null;
        }
        return getWordWithMissingChar(start, end, pos, missingChar);
    }

    public boolean isOccupiedOrAdjacentOccupied(Pos p) {
        if (!isValid(p)) {
            return false;
        }
        return !tiles[p.r][p.c].isAbsent() || isOccupiedE(p) || isOccupiedS(p) || isOccupiedN(p) || isOccupiedW(p);
    }

    public boolean isOccupiedAndValid(Pos p) {
        if (p.r < 0 || p.r >= N || p.c < 0 || p.c >= N) {
            return false;
        }
        return !tiles[p.r][p.c].isAbsent();
    }

    public boolean isOccupied(Pos p) {
        return !tiles[p.r][p.c].isAbsent();
    }

    public boolean isOccupiedE(Pos p) {
        return p.c + 1 < N && !tiles[p.r][p.c + 1].isAbsent();
    }

    public boolean isOccupiedW(Pos p) {
        return p.c - 1 >= 0 && !tiles[p.r][p.c - 1].isAbsent();
    }

    public boolean isOccupiedS(Pos p) {
        return p.r + 1 < N && !tiles[p.r + 1][p.c].isAbsent();
    }

    public boolean isOccupiedN(Pos p) {
        return p.r - 1 >= 0 && !tiles[p.r - 1][p.c].isAbsent();
    }

    /**
     * Starting at position "start", going in direction "dir" at most "maxLen" distance,
     * find the first tile that is occupied or an adjacent tile in any direction is occupied.
     */
    public Pos getFirstOccupiedOrAdjacent(Pos start, Dir dir, int maxLen) {

        Pos p = start;
        for (int i = 0; i < maxLen; i++, p = p.go(dir)) {
            if (!isValid(p)) {
                return null;
            }
            if (isOccupiedOrAdjacentOccupied(p)) {
                return p;
            }
        }
        return null;
    }

    public Pos getEndOfOccupied(Pos start, Dir dir) {
        switch (dir) {
            case E: return getEndOfOccupiedE(start);
            case S: return getEndOfOccupiedS(start);
            case W: return getEndOfOccupiedW(start);
            case N: return getEndOfOccupiedN(start);
        }
        throw new IllegalStateException("Invalid dir: " + dir);
    }

    public Pos getEndOfOccupiedE(Pos p) {
        do {
            p = p.e();
        } while (p.c < N && !tiles[p.r][p.c].isAbsent());
        return p.w();
    }

    public Pos getEndOfOccupiedW(Pos p) {
        do {
            p = p.w();
        } while (p.c >= 0 && !tiles[p.r][p.c].isAbsent());
        return p.e();
    }

    public Pos getEndOfOccupiedS(Pos p) {
        do {
            p = p.s();
        } while (p.r < N && !tiles[p.r][p.c].isAbsent());
        return p.n();
    }

    public Pos getEndOfOccupiedN(Pos p) {
        do {
            p = p.n();
        } while (p.r >= 0 && !tiles[p.r][p.c].isAbsent());
        return p.s();
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
