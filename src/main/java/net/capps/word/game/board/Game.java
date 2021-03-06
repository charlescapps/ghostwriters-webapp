package net.capps.word.game.board;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.Rack;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.LetterPoints;
import net.capps.word.game.tile.Tile;
import net.capps.word.rest.models.ErrorModel;
import net.capps.word.rest.models.GameModel;
import net.capps.word.rest.models.MoveModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/12/15.
 */
public class Game {
    private static final LetterPoints letterPoints = LetterPoints.getInstance();

    // --------- Errors ---------
    private static final Optional<ErrorModel> ERR_CANNOT_PLAY = Optional.of(new ErrorModel("You can't play a move, game is complete!"));
    private static final Optional<String> ERR_CANNOT_PLAY_GRABBED_TILES = Optional.of("You can't play the letters you just grabbed in the same place!");

    private final int gameId;
    private final SpecialDict specialDict;
    private final int N;
    private final SquareSet squareSet;
    private final TileSet tileSet;
    private Rack player1Rack;
    private Rack player2Rack;
    private int player1Points;
    private int player2Points;
    private boolean player1Turn;
    private GameResult gameResult;
    private Optional<Move> previousMoveOpt;


    public Game(GameModel gameModel, Optional<Move> previousMoveOpt) throws Exception {
        Preconditions.checkNotNull(gameModel);
        Preconditions.checkNotNull(gameModel.getBoardSize());
        Preconditions.checkNotNull(gameModel.getTiles());
        Preconditions.checkNotNull(gameModel.getSquares());
        this.gameId = Preconditions.checkNotNull(gameModel.getId());
        this.specialDict = gameModel.getSpecialDict();
        Reader squareReader = new StringReader(gameModel.getSquares());
        Reader tileReader = new StringReader(gameModel.getTiles());
        N = gameModel.getBoardSize().getN();
        squareSet = new SquareSet(N);
        tileSet = new TileSet(N);
        load(squareReader, tileReader);
        player1Rack = new Rack(gameModel.getPlayer1Rack());
        player2Rack = new Rack(gameModel.getPlayer2Rack());
        player1Points = gameModel.getPlayer1Points();
        player2Points = gameModel.getPlayer2Points();
        player1Turn = gameModel.getPlayer1Turn();
        gameResult = gameModel.getGameResult();
        this.previousMoveOpt = previousMoveOpt;
    }

    public Game(int gameId, GameResult gameResult, TileSet tileSet, SquareSet squareSet, String player1Rack, String player2Rack,
                int player1Points, int player2Points, boolean player1Turn, Optional<Move> previousMoveOpt) {
        this.gameId = gameId;
        this.specialDict = null;
        this.squareSet = Preconditions.checkNotNull(squareSet);
        this.tileSet = Preconditions.checkNotNull(tileSet);
        this.gameResult = Preconditions.checkNotNull(gameResult);
        this.player1Rack = new Rack(player1Rack);
        this.player2Rack = new Rack(player2Rack);
        this.player1Points = player1Points;
        this.player2Points = player2Points;
        this.player1Turn = player1Turn;
        this.N = tileSet.N;
        this.previousMoveOpt = previousMoveOpt;
    }

    public void load(Reader squareReader, Reader tileReader) throws IOException, InvalidBoardException {
        squareSet.load(squareReader);
        tileSet.load(tileReader);
    }

    public int getGameId() {
        return gameId;
    }

    public SpecialDict getSpecialDict() {
        return specialDict;
    }

    public int getN() {
        return N;
    }

    public SquareSet getSquareSet() {
        return squareSet;
    }

    public TileSet getTileSet() {
        return tileSet;
    }

    public Rack getPlayer1Rack() {
        return player1Rack;
    }

    public Rack getPlayer2Rack() {
        return player2Rack;
    }

    public int getPlayer1Points() {
        return player1Points;
    }

    public int getPlayer2Points() {
        return player2Points;
    }

    public boolean isPlayer1Turn() {
        return player1Turn;
    }

    public Rack getCurrentPlayerRack() {
        return player1Turn ? player1Rack : player2Rack;
    }

    public Rack getOpponentPlayerRack() {
        return player1Turn ? player2Rack : player1Rack;
    }

    public GameResult getGameResult() {
        return gameResult;
    }

    public Optional<ErrorModel> getMoveError(Move move) {
        if (gameResult != GameResult.IN_PROGRESS && gameResult != GameResult.OFFERED) {
            return ERR_CANNOT_PLAY;
        }
        switch (move.getMoveType()) {
            case PLAY_WORD:
                return getPlayWordError(move);
            case GRAB_TILES:
                return getGrabTilesError(move).map(errMsg -> new ErrorModel(errMsg));
            case PASS:
                return Optional.empty();
            case RESIGN:
                return Optional.empty();
        }
        throw new IllegalStateException();
    }

    public Optional<String> getReplayGrabbedTilesError(MoveModel moveModel, List<MoveModel> prevMoves) {
        if (moveModel.getMoveType() != MoveType.PLAY_WORD) {
            return Optional.empty();
        }
        if (prevMoves.size() < 2) {
            return Optional.empty();
        }

        MoveModel prevMove = prevMoves.get(0);
        MoveModel prevPrevMove = prevMoves.get(1);
        final int currentPlayerId = moveModel.getPlayerId();

        // Check if player is playing the the same tiles they grabbed 2 turns ago.
        if (prevPrevMove.getPlayerId() == currentPlayerId &&
                prevPrevMove.getMoveType() == MoveType.GRAB_TILES &&
                prevMove.getPlayerId() != currentPlayerId) {

            if (didPlaySameTilesAsGrab(prevPrevMove, moveModel)) {
                return ERR_CANNOT_PLAY_GRABBED_TILES;
            }
        }

        return Optional.empty();
    }
    
    private boolean didPlaySameTilesAsGrab(MoveModel grabMove, MoveModel playMove) {

        Pair<Pos, Pos> startAndEnd = getStartAndEndOfPlayTiles(playMove);
        if (startAndEnd == null) {
            return false;
        }

        if (startAndEnd.getLeft().equals(grabMove.getStart().toPos()) &&
                playMove.getDir() == grabMove.getDir() &&
                playMove.getTiles().equals(grabMove.getLetters())) {
            return true;
        }

        if (playMove.getDir().negate() == grabMove.getDir()) {
            final Pos end = startAndEnd.getRight();
            if (end.equals(grabMove.getStart().toPos())) {
                final String reverseTiles = new StringBuilder(playMove.getTiles()).reverse().toString();
                if (reverseTiles.equals(grabMove.getLetters())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Pair<Pos, Pos> getStartAndEndOfPlayTiles(MoveModel moveModel) {
        final Pos start = moveModel.getStart().toPos();
        final Dir dir = moveModel.getDir();

        Pos p = start;
        while (tileSet.isOccupiedAndValid(p)) {
            p = p.go(dir);
        }

        if (!tileSet.isValid(p)) {
            return null;
        }

        final Pos startPlayTiles = p;

        final int tilesLen = moveModel.getTiles().length();

        for (int i = 0; i < tilesLen - 1; ++i) {
            p = p.go(dir);
            if (!tileSet.isValid(p) || tileSet.isOccupied(p)) {
                return null;
            }
        }

        return ImmutablePair.of(startPlayTiles, p);
    }

    /**
     * Return the number of points, and the special dictionary words played.
     * @param validatedMove
     * @return
     */
    public PlayResult playMove(Move validatedMove) {
        if (gameResult != GameResult.IN_PROGRESS && gameResult != GameResult.OFFERED) {
            throw new IllegalStateException("Can't play a move for a finished game!");
        }
        switch (validatedMove.getMoveType()) {
            case PLAY_WORD:
                return playWordMove(validatedMove);
            case GRAB_TILES:
                playGrabTilesMove(validatedMove);
                return PlayResult.NON_PLAY_WORD_RESULT;
            case PASS:
                playPassMove(validatedMove);
                return PlayResult.NON_PLAY_WORD_RESULT;
            case RESIGN:
                playResignMove(validatedMove);
                return PlayResult.NON_PLAY_WORD_RESULT;
            default:
                throw new IllegalStateException();
        }
    }

    public int computeStandardPoints(Move move) {
        if (move.getMoveType() != MoveType.PLAY_WORD) {
            return 0;
        }
        int perpWordPoints = 0;
        int wordPoints = 0;
        String word = move.getLetters();
        Pos start = move.getStart();
        Dir dir = move.getDir();

        // Compute the points
        Pos p = start;
        for (int i = 0; i < word.length(); i++, p = p.go(dir)) {
            char c = word.charAt(i);
            Tile tile = tileSet.get(p);
            // If there was no tile already on the board, then include letter/word bonuses
            if (tile.isAbsent()) {
                Square square = squareSet.get(p);
                wordPoints += letterPoints.getPointValue(c) * square.getLetterMultiplier();
                perpWordPoints += computePerpWordPoints(p, dir, c, square.getLetterMultiplier());
            }
            // If the tile was already on the board, just include the base point value.
            else {
                wordPoints += letterPoints.getPointValue(c);
            }
        }

        int totalPoints = wordPoints + perpWordPoints;
        if (totalPoints <= 0) {
            throw new IllegalStateException("Something went wrong computing the points - any move must earn > 0 points!");
        }
        return totalPoints;
    }

    /**
     * Precondition: move.getMoveType == MoveType.PLAY_WORD_MOVE
     */
    public List<String> getSpecialWordsPlayed(Move move) {
        if (specialDict == null) {
            return ImmutableList.of();
        }
        List<String> specialWords = new ArrayList<>();

        final String word = move.getLetters();
        // Add the primary word played, iff it's in the Special Dictionary for this game.
        if (specialDict.contains(word)) {
            specialWords.add(word);
        }

        final Pos start = move.getStart();
        final Dir dir = move.getDir();

        // Compute the points
        Pos p = start;
        for (int i = 0; i < word.length(); ++i, p = p.go(dir)) {
            char c = word.charAt(i);
            Tile tile = tileSet.get(p);
            // If there was no tile already on the board, then check the perpendicular word
            if (tile.isAbsent()) {
                addSpecialDictWordFromPerpWord(specialWords, p, dir, c);
            }
        }

        return specialWords;
    }

    private int computePerpWordPoints(Pos baseWordPos, Dir d, char playChar, int letterScale) {
        final Dir perp = d.perp();
        Pos end = tileSet.getEndOfOccupied(baseWordPos, perp);
        Pos p = tileSet.getEndOfOccupied(baseWordPos, perp.negate());
        if (p.equals(end)) {
            return 0; // There's no perpendicular word
        }
        end = end.go(perp); // 1 tile after the end
        int wordPoints = 0;
        for (; !p.equals(end); p = p.go(perp)) {
            if (p.equals(baseWordPos)) {
                wordPoints += letterPoints.getPointValue(playChar) * letterScale;
            } else {
                final char c = tileSet.getLetterAt(p);
                wordPoints += letterPoints.getPointValue(c);
            }
        }
        return wordPoints;
    }

    private void addSpecialDictWordFromPerpWord(List<String> specialWords, Pos baseWordPos, Dir d, char playChar) {
        if (specialDict == null) {
            return;
        }
        final Dir perp = d.perp();
        Pos end = tileSet.getEndOfOccupied(baseWordPos, perp);
        Pos p = tileSet.getEndOfOccupied(baseWordPos, perp.negate());
        if (p.equals(end)) {
            return; // There's no perpendicular word
        }
        String perpWord = tileSet.getWordWithMissingChar(p, end, baseWordPos, playChar);
        if (specialDict.contains(perpWord)) {
            specialWords.add(perpWord);
        }
    }

    private PlayResult playWordMove(Move validatedMove) {
        // Compute the base points earned from words formed on board + bonus squares
        int points = computeStandardPoints(validatedMove);

        // Get the list of "special" dictionary words played
        List<String> specialWordsPlayed = getSpecialWordsPlayed(validatedMove);

        // Award a bonus if a special dictionary word is played -- players can't earn this bonus twice w/ one play
        if (!specialWordsPlayed.isEmpty()) {
            points += specialDict.getDictType().getBonusPoints();
        }

        tileSet.playWordMove(validatedMove);
        getCurrentPlayerRack().removeTiles(validatedMove.getTiles());
        if (player1Turn) {
            player1Points += points;
        } else {
            player2Points += points;
        }

        // Check for game over, if opponent has no tiles and board is all stone
        gameResult = checkForGameEnd(validatedMove);

        player1Turn = !player1Turn;

        previousMoveOpt = Optional.of(validatedMove);
        return new PlayResult(points, specialWordsPlayed);
    }

    private void playGrabTilesMove(Move validatedMove) {
        tileSet.playGrabTilesMove(validatedMove);
        getCurrentPlayerRack().addTiles(validatedMove.getTiles());

        // Check for game over, if opponent has no tiles and board is all stone
        gameResult = checkForGameEnd(validatedMove);

        player1Turn = !player1Turn;
        previousMoveOpt = Optional.of(validatedMove);
    }

    private void playPassMove(Move validatedMove) {
        gameResult = checkForGameEnd(validatedMove);
        player1Turn = !player1Turn;
        previousMoveOpt = Optional.of(validatedMove);
    }

    private void playResignMove(Move validatedMove) {
        gameResult = player1Turn ? GameResult.PLAYER1_RESIGN : GameResult.PLAYER2_RESIGN;
        player1Turn = !player1Turn;
        previousMoveOpt = Optional.of(validatedMove);
    }

    private GameResult checkForGameEnd(Move validatedMove) {
        switch (validatedMove.getMoveType()) {
            case GRAB_TILES:
            case PLAY_WORD:
                // End game if the opponent's rack is empty and all tiles are played
                if (player1Turn && !player2Rack.hasPlayableTile() ||
                        !player1Turn && !player1Rack.hasPlayableTile()) {
                    if (tileSet.areAllTilesPlayed()) {
                        return computeGameResultFromFinalPoints();
                    }
                }
                return gameResult;
            case PASS:
                if (previousMoveOpt.isPresent()) {
                    Move previousMove = previousMoveOpt.get();
                    // If 2 passes happen in a row, then the game is over.
                    if (previousMove.getMoveType() == MoveType.PASS) {
                        return computeGameResultFromFinalPoints();
                    }
                }
                // If the current player is passing,
                // and the board has no more grabbable tiles,
                // and opponent's rack is empty,
                // then the game is over
                if ((isPlayer1Turn() && !player2Rack.hasPlayableTile() || !isPlayer1Turn() && !player1Rack.hasPlayableTile())
                        && tileSet.areAllTilesPlayed()) {
                    return computeGameResultFromFinalPoints();
                }
                return gameResult;
            default:
                throw new IllegalStateException("Impossible; all move types covered.");
        }
    }

    private GameResult computeGameResultFromFinalPoints() {
        if (player1Points > player2Points) {
            return GameResult.PLAYER1_WIN;
        } else if (player1Points < player2Points) {
            return GameResult.PLAYER2_WIN;
        }
        return GameResult.TIE;
    }

    private Optional<ErrorModel> getPlayWordError(Move move) {
        // Check that the player has the requisite tiles in their rack.
        if (player1Turn && !player1Rack.hasTiles(move.getTiles())) {
            return Optional.of(new ErrorModel(format("Player 1 doesn't have required tiles: \"%s\"", move.getTilesAsString())));
        } else if (!player1Turn && !player2Rack.hasTiles(move.getTiles())) {
            return Optional.of(new ErrorModel(format("Player 2 doesn't have required tiles: \"%s\"", move.getTilesAsString())));
        }
        // Check that the play is valid
        return tileSet.getPlayWordMoveError(move, specialDict);
    }

    private Optional<String> getGrabTilesError(Move move) {
        // Check that the player's tiles won't exceed the maximum number of tiles in hand.
        if (player1Turn && !player1Rack.canAddTiles(move.getTiles())) {
            return Optional.of(format("You can't grab the tiles, \"%s\". You can only hold %d tiles!",
                    move.getTilesAsString(),
                    Rack.MAX_TILES_IN_RACK));
        } else if (!player1Turn && !player2Rack.canAddTiles(move.getTiles())) {
            return Optional.of(format("You can't grab the tiles, \"%s\". You can only hold %d tiles!",
                    move.getTilesAsString(),
                    Rack.MAX_TILES_IN_RACK));
        }
        // Check that the play is valid
        return tileSet.isValidGrabTilesMove(move);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("gameId", gameId)
                .add("player1Turn", player1Turn)
                .add("gameResult", gameResult)
                .add("player1Points", player1Points)
                .add("player1Rack", player1Rack)
                .add("player2Points", player2Points)
                .add("player2Rack", player2Rack)
                .add("tileSet", tileSet)
                .toString();
    }

}
