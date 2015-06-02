package net.capps.word.game.board;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.Rack;
import net.capps.word.game.dict.DictType;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.LetterPoints;
import net.capps.word.game.tile.Tile;
import net.capps.word.rest.models.GameModel;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/12/15.
 */
public class Game {
    private static final LetterPoints letterPoints = LetterPoints.getInstance();

    // --------- Errors ---------
    private static final Optional<String> ERR_CANNOT_PLAY = Optional.of("Cannot play more moves, game is complete!");

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

    public Optional<String> getMoveError(Move move) {
        if (gameResult != GameResult.IN_PROGRESS && gameResult != GameResult.OFFERED) {
            return ERR_CANNOT_PLAY;
        }
        switch (move.getMoveType()) {
            case PLAY_WORD:
                return getPlayWordError(move);
            case GRAB_TILES:
                return getGrabTilesError(move);
            case PASS:
                return Optional.empty();
        }
        throw new IllegalStateException();
    }

    public int playMove(Move validatedMove) {
        if(gameResult != GameResult.IN_PROGRESS && gameResult != GameResult.OFFERED) {
            throw new IllegalStateException("Can't play a move for a finished game!");
        }
        switch (validatedMove.getMoveType()) {
            case PLAY_WORD:
                return playWordMove(validatedMove);
            case GRAB_TILES:
                return playGrabTilesMove(validatedMove);
            case PASS:
                return playPassMove(validatedMove);
            default:
                throw new IllegalStateException();
        }
    }

    public int computePoints(Move move) {
        if (move.getMoveType() != MoveType.PLAY_WORD) {
            return 0;
        }
        int perpWordPoints = 0;
        int wordPoints = 0;
        String word = move.getLetters();
        Pos start = move.getStart();
        Dir dir = move.getDir();

        // Compute the points
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Pos p = start.go(dir, i);
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

        int totalPoints = wordPoints + perpWordPoints + computeBonusPointsForSpecialDictionaryPlay(move);
        if (totalPoints <= 0) {
            throw new IllegalStateException("Something went wrong computing the points - any move must earn > 0 points!");
        }
        return totalPoints;
    }

    private int computeBonusPointsForSpecialDictionaryPlay(Move move) {
        final String word = move.getLetters();
        if (specialDict == null) {
            return 0;
        }
        DictType primaryDict = specialDict.getPrimaryDict();
        if (primaryDict.getDictionarySet().contains(word)) {
            return primaryDict.getBonusPoints();
        }

        return 0;
    }

    private int computePerpWordPoints(Pos baseWordPos, Dir d, char playChar, int letterScale) {
        final Dir perp = d.perp();
        final Pos end = tileSet.getEndOfOccupied(baseWordPos.go(perp), perp);
        final Pos start = tileSet.getEndOfOccupied(baseWordPos.go(perp.negate()), perp.negate());
        if (start.equals(end)) {
            return 0; // There's no perpendicular word
        }
        final Pos afterEnd = end.go(perp);
        int wordPoints = 0;
        for (Pos p = start; !p.equals(afterEnd); p = p.go(perp)) {
            if (p.equals(baseWordPos)) {
                wordPoints += letterPoints.getPointValue(playChar) * letterScale;
            } else {
                final char c = tileSet.getLetterAt(p);
                wordPoints += letterPoints.getPointValue(c);
            }
        }
        return wordPoints;
    }

    private boolean doChangeTurn() {
        // If there are tiles left to grab, OR the other player's rack isn't empty, then swap whose turn it is
        // Otherwise, it remains the same player's turn.
        boolean areAllTilesPlayed = tileSet.areAllTilesPlayed();
        return !areAllTilesPlayed || !getOpponentPlayerRack().isEmpty() || gameResult != GameResult.IN_PROGRESS;
    }


    private int playWordMove(Move validatedMove) {
        int numPoints = computePoints(validatedMove);
        tileSet.playWordMove(validatedMove, squareSet);
        getCurrentPlayerRack().removeTiles(validatedMove.getTiles());
        if (player1Turn) {
            player1Points += numPoints;
        } else {
            player2Points += numPoints;
        }
        gameResult = checkForGameEnd(validatedMove);

        if (doChangeTurn()) {
            player1Turn = !player1Turn;
        }

        previousMoveOpt = Optional.of(validatedMove);
        return numPoints;
    }

    private int playGrabTilesMove(Move validatedMove) {
        tileSet.playGrabTilesMove(validatedMove);
        getCurrentPlayerRack().addTiles(validatedMove.getTiles());
        if (doChangeTurn()) {
            player1Turn = !player1Turn;
        }
        previousMoveOpt = Optional.of(validatedMove);
        return 0; // 0 points for a grab move.
    }

    private int playPassMove(Move validatedMove) {
        gameResult = checkForGameEnd(validatedMove);
        player1Turn = !player1Turn;
        previousMoveOpt = Optional.of(validatedMove);
        return 0;
    }

    private GameResult checkForGameEnd(Move validatedMove) {
        switch (validatedMove.getMoveType()) {
            case GRAB_TILES:
                return gameResult; // Game cannot end immediately after grabbing tiles
            case PLAY_WORD:
                // End game if both player's racks are empty and all tiles are played
                if (player1Rack.isEmpty() && player2Rack.isEmpty()) {
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
                // and the board has no more grabbable tiles
                // opponent's rack is empty
                // then the game is over
                if (isPlayer1Turn() && player2Rack.isEmpty() && tileSet.areAllTilesPlayed() ||
                   !isPlayer1Turn() && player1Rack.isEmpty() && tileSet.areAllTilesPlayed()) {
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

    private Optional<String> getPlayWordError(Move move) {
        // Check that the player has the requisite tiles in their rack.
        if (player1Turn && !player1Rack.hasTiles(move.getTiles())) {
            return Optional.of(format("Player 1 doesn't have required tiles: \"%s\"", move.getTilesAsString()));
        } else if (!player1Turn && !player2Rack.hasTiles(move.getTiles())) {
            return Optional.of(format("Player 2 doesn't have required tiles: \"%s\"", move.getTilesAsString()));
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
