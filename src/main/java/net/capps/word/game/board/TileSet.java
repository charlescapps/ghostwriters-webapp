package net.capps.word.game.board;

import com.google.common.base.Preconditions;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.*;
import net.capps.word.game.dict.Dictionaries;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.LetterUtils;
import net.capps.word.game.tile.RackTile;
import net.capps.word.game.tile.Tile;

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

    public Tile get(MutPos mp) {
        return tiles[mp.r][mp.c];
    }

    public void set(Pos p, Tile tile) {
        tiles[p.r][p.c] = tile;
    }

    public void set(MutPos p, Tile tile) {
        tiles[p.r][p.c] = tile;
    }

    public boolean isValid(Pos p) {
        return p.r >= 0 && p.r < N && p.c >= 0 && p.c < N;
    }

    public boolean isValid(MutPos p) {
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

    public char getLetterAt(MutPos p) {
        Tile tile = tiles[p.r][p.c];
        return tile.getLetter();
    }

    public String getWordWithMissingChar(MutPos start, MutPos end, MutPos middle, char middleChar) {
        int diff = end.minus(start);
        Dir dir = start.getDirTo(end);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= diff; ++i) {
            if (start.equals(middle)) {
                sb.append(middleChar);
            } else {
                sb.append(getLetterAt(start));
            }
            start.go(dir);
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

    public void playWordMove(Move move) {
        Preconditions.checkArgument(move.getMoveType() == MoveType.PLAY_WORD, "Move type must be Play Word.");
        Dir dir = move.getDir();
        Pos start = move.getStart();
        List<RackTile> tilesPlayed = move.getTiles();
        String word = move.getLetters();

        int rackIndex = 0;
        MutPos mp = start.toMutPos();
        for (int i = 0; i < word.length(); i++, mp.go(dir)) {
            char letter = word.charAt(i);
            Tile existing = get(mp);
            if (existing.isAbsent()) {
                RackTile rackTile = tilesPlayed.get(rackIndex++);
                set(mp, rackTile.toTile(letter));
            } else {
                if (existing.getLetter() != letter) {
                    throw new IllegalStateException("Attempting to place invalid move: " + move);
                }
                set(mp, Tile.playedTile(letter)); // Any tile that is part of the played word is now "set in stone"
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
        MutPos mp = start.toMutPos();
        for (int i = 0; i < wordLen; ++i, mp.go(dir)) {
            Tile existing = get(mp);
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
            set(mp, Tile.absentTile());
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

    public Optional<String> getPlayWordMoveError(Move move, SpecialDict specialDict) {
        // Check if the tiles played from the player's Rack match what's on the board
        Optional<String> errorOpt = lettersMatchTilesPlayed(move.getStart(), move.getDir(), move.getLetters(), move.getTiles());
        if (errorOpt.isPresent()) {
            return errorOpt;
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
        MutPos mp = start.toMutPos();
        for (int i = 0; i < letters.length(); i++, mp.go(dir)) {
            char c = letters.charAt(i);
            Tile tile = get(mp);
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
        MutPos mp = start.toMutPos();
        for (int i = 0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            Tile tile = get(mp);
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
            mp.go(dir);
        }

        return rackIndex == tiles.size();
    }

    private Optional<String> lettersMatchTilesGrabbed(final Pos start, final Dir dir, final String letters, List<RackTile> tiles) {
        if (tiles.size() != letters.length()) {
            return Optional.of(format("The letters \"%s\" don't match the number of tiles grabbed, %d", letters, tiles.size()));
        }
        MutPos mp = start.toMutPos();
        for (int i = 0; i < letters.length(); i++, mp.go(dir)) {
            char c = letters.charAt(i);
            Tile tile = get(mp);
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

    public Optional<String> getPlacementError(Placement placement, SpecialDict specialDict) {
        final String word = placement.getWord();

        // Must be a valid dictionary word
        if (!isValidWord(word, specialDict)) {
            return Optional.of(format("\"%s\" is not in the dictionary for this game!", word));
        }

        final Dir dir = placement.getDir();

        // Can only play EAST or SOUTH
        if (!dir.isValidPlayDir()) {
            return Optional.of(format("%s is not a valid direction to play. Can only play South or East.", dir));
        }

        Optional<String> errorOpt = getInvalidPositionError(placement.getStart(), placement.getDir());
        if (errorOpt.isPresent()) {
            return errorOpt;
        }

        // Must be a valid play in the primary direction
        errorOpt = getErrorForPrimaryDir(placement);
        if (errorOpt.isPresent()) {
            return errorOpt;
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

        MutPos mp = start.toMutPos();
        for (int i = 0; i < word.length(); ++i, mp.go(dir)) {

            // Placement can't go off end of board
            if (!isValid(mp)) {
                return Optional.of("Play would go off the board!");
            }

            // Letter must match occupied tiles
            if (!tiles[mp.r][mp.c].isAbsent()) {
                if (getLetterAt(mp) != word.charAt(i)) {
                    return Optional.of("Word played must match existing tiles!");
                }
            }
        }

        if (isOccupiedAndValid(mp)) {
            return Optional.of("Letters given to play must include all consecutive letters on the board from start position.");
        }
        return Optional.empty();
    }

    private boolean isValidPlayInPrimaryDir(Placement placement) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
        final Dir dir = placement.getDir();

        MutPos mp = start.toMutPos();
        for (int i = 0; i < word.length(); ++i, mp.go(dir)) {
            // Placement can't go off end of board
            if (!isValid(mp)) {
                return false;
            }

            // Letter must match occupied tiles
            if (!tiles[mp.r][mp.c].isAbsent()) {
                if (tiles[mp.r][mp.c].getLetter() != word.charAt(i)) {
                    return false;
                }
            }
        }

        return !isOccupiedAndValid(mp);
    }

    private Optional<String> getErrorForPerpendicularPlacement(final Placement placement, final SpecialDict specialDict) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
        final Dir dir = placement.getDir();

        MutPos mp = new MutPos(start);
        for (int i = 0; i < word.length(); ++i, mp.go(dir)) {
            // Don't need to check already occupied squares.
            if (!tiles[mp.r][mp.c].isAbsent()) {
                continue;
            }
            char c = word.charAt(i);
            String perpWord = getPerpWordForAttemptedPlacement(mp, c, dir);
            if (perpWord != null) {
                if (!isValidWord(perpWord, specialDict)) {
                    final String msg = "\"" + perpWord + "\" isn't in this game's dictionary.";
                    return Optional.of(msg);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * ASSUMPTION: We already checked the placement is valid in the primary direction.
     */
    private boolean isValidPerpendicularPlacement(final Placement placement, final SpecialDict specialDict) {
        final String word = placement.getWord();
        final Pos start = placement.getStart();
        final Dir dir = placement.getDir();

        final int wordLen = word.length();
        MutPos mp = start.toMutPos();
        for (int i = 0; i < wordLen; ++i, mp.go(dir)) {
            // Don't need to check already occupied squares.
            if (!tiles[mp.r][mp.c].isAbsent()) {
                continue;
            }
            char c = word.charAt(i);
            String perpWord = getPerpWordForAttemptedPlacement(mp, c, dir);
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
    private String getPerpWordForAttemptedPlacement(final MutPos pos, char missingChar, Dir dir) {
        // Precondition: pos isn't an occupied tile.
        switch (dir) {
            case E:
            case W:
                MutPos start = getEndOfOccupiedAfter(new MutPos(pos), Dir.N);
                MutPos end = getEndOfOccupiedAfter(new MutPos(pos), Dir.S);
                if (start.equals(end)) {
                    return null;
                }
                return getWordWithMissingChar(start, end, pos, missingChar);

            case S:
            case N:
                start = getEndOfOccupiedAfter(new MutPos(pos), Dir.W);
                end = getEndOfOccupiedAfter(new MutPos(pos), Dir.E);
                if (start.equals(end)) {
                    return null;
                }
                return getWordWithMissingChar(start, end, pos, missingChar);
        }
        return null;
    }

    public boolean isOccupiedOrAdjacentOccupied(MutPos p) {
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

    public boolean isOccupiedAndValid(MutPos p) {
        if (p.r < 0 || p.r >= N || p.c < 0 || p.c >= N) {
            return false;
        }
        return !tiles[p.r][p.c].isAbsent();
    }

    public boolean isOccupied(MutPos p) {
        return !tiles[p.r][p.c].isAbsent();
    }

    public boolean isOccupiedE(MutPos p) {
        return p.c + 1 < N && !tiles[p.r][p.c + 1].isAbsent();
    }

    public boolean isOccupiedW(MutPos p) {
        return p.c - 1 >= 0 && !tiles[p.r][p.c - 1].isAbsent();
    }

    public boolean isOccupiedS(MutPos p) {
        return p.r + 1 < N && !tiles[p.r + 1][p.c].isAbsent();
    }

    public boolean isOccupiedN(MutPos p) {
        return p.r - 1 >= 0 && !tiles[p.r - 1][p.c].isAbsent();
    }

    /**
     * Starting at position "start", going in direction "dir" at most "maxLen" distance,
     * find the first tile that is occupied or an adjacent tile in any direction is occupied.
     */
    public MutPos getFirstOccupiedOrAdjacent(Pos start, Dir dir, int maxLen) {

        MutPos mp = start.toMutPos();
        for (int i = 0; i < maxLen; i++, mp.go(dir)) {
            if (!isValid(mp)) {
                return null;
            }
            if (isOccupiedOrAdjacentOccupied(mp)) {
                return mp;
            }
        }
        return null;
    }

    public MutPos getEndOfOccupiedAfter(Pos start, Dir dir) {
        MutPos mp = new MutPos(start);
        do {
            mp.go(dir);
        } while (isOccupiedAndValid(mp));
        mp.go(dir, -1);
        return mp;
    }

    /**
     * Modify the given mutable position to be pointing at the last occupied position, starting at the
     * original position, going in the given direction.
     *
     * @param start
     * @param dir
     * @return
     */
    public MutPos getEndOfOccupiedAfter(MutPos start, Dir dir) {
        do {
            start.go(dir);
        } while (isOccupiedAndValid(start));
        start.go(dir, -1);
        return start;
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
