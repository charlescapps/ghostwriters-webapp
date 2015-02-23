package net.capps.word.game.board;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.*;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.LetterPoints;
import net.capps.word.game.tile.Tile;
import net.capps.word.rest.models.GameModel;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/12/15.
 */
public class GameState {
    private final int gameId;
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


    public GameState(GameModel gameModel, Optional<Move> previousMoveOpt) throws Exception {
        Preconditions.checkNotNull(gameModel);
        Preconditions.checkNotNull(gameModel.getBoardSize());
        Preconditions.checkNotNull(gameModel.getTiles());
        Preconditions.checkNotNull(gameModel.getSquares());
        this.gameId = Preconditions.checkNotNull(gameModel.getId());
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

    public GameState(int gameId, GameResult gameResult, TileSet tileSet, SquareSet squareSet, String player1Rack, String player2Rack,
                     int player1Points, int player2Points, boolean player1Turn, Optional<Move> previousMoveOpt) {
        this.gameId = gameId;
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

    public GameResult getGameResult() {
        return gameResult;
    }

    public Optional<String> isValidMove(Move move) {
        if (gameResult != GameResult.IN_PROGRESS) {
            return Optional.of("Cannot play more moves, game is complete!");
        }
        switch (move.getMoveType()) {
            case PLAY_WORD:
                return isValidPlayWordMove(move);
            case GRAB_TILES:
                return isValidGrabTilesMove(move);
            case PASS:
                return Optional.absent();
        }
        throw new IllegalStateException();
    }

    public int playMove(Move validatedMove) {
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
        int wordPoints = 0;
        int wordScale = 1;
        String word = move.getLetters();
        Pos start = move.getStart();
        Dir dir = move.getDir();
        LetterPoints letterPoints = LetterPoints.getInstance();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Pos p = start.go(dir, i);
            Tile tile = tileSet.get(p);
            // If there was no tile already on the board, then include letter/word bonuses
            if (tile.isAbsent()) {
                Square square = squareSet.get(p);
                wordPoints += letterPoints.getPointValue(c) * square.getLetterMultiplier();
                wordScale *= square.getWordMultiplier();
            }
            // If the tile was already on the board, just include the base point value.
            else {
                wordPoints += letterPoints.getPointValue(c);
            }
        }

        int totalPoints = wordPoints * wordScale;
        if (totalPoints <= 0) {
            throw new IllegalStateException("Something went wrong computing the points - any move must earn > 0 points!");
        }
        return totalPoints;
    }

    private int playWordMove(Move validatedMove) {
        int numPoints = computePoints(validatedMove);
        tileSet.playWordMove(validatedMove);
        getCurrentPlayerRack().removeTiles(validatedMove.getTiles());
        if (player1Turn) {
            player1Points += numPoints;
        } else {
            player2Points += numPoints;
        }
        gameResult = checkForGameEnd();
        player1Turn = !player1Turn;
        previousMoveOpt = Optional.of(validatedMove);
        return numPoints;
    }

    private int playPassMove(Move validatedMove) {
        Preconditions.checkArgument(validatedMove.getMoveType() == MoveType.PASS);
        if (previousMoveOpt.isPresent()) {
            Move previousMove = previousMoveOpt.get();
            if (previousMove.getMoveType() == MoveType.PASS) {
                if (player1Points > player2Points) {
                    gameResult = GameResult.PLAYER1_WIN;
                } else if (player2Points > player1Points) {
                    gameResult = GameResult.PLAYER2_WIN;
                } else {
                    gameResult = GameResult.DRAW;
                }
            }
        }
        player1Turn = !player1Turn;
        previousMoveOpt = Optional.of(validatedMove);
        return 0;
    }

    private GameResult checkForGameEnd() {
        // Possible end game if it's player 1's turn and rack is empty
        if (player1Turn && player1Rack.isEmpty()) {
            boolean allTilesArePlayed = tileSet.areAllTilesPlayed();
            if (!allTilesArePlayed) {
                return GameResult.IN_PROGRESS;
            }
            player1Points += player2Rack.getSumOfPoints();

        }
        // Also possible end game if player 2 turn and rack is empty
        else if (!player1Turn && player2Rack.isEmpty()) {
            boolean allTilesArePlayed = tileSet.areAllTilesPlayed();
            if (!allTilesArePlayed) {
                return GameResult.IN_PROGRESS;
            }
            player2Points += player1Rack.getSumOfPoints();
        }
        // If the current player's rack is not empty, the game isn't over yet.
        else {
            return GameResult.IN_PROGRESS;
        }
        if (player1Points > player2Points) {
            return GameResult.PLAYER1_WIN;
        } else if (player1Points < player2Points) {
            return GameResult.PLAYER2_WIN;
        }
        return GameResult.DRAW;
    }

    private int playGrabTilesMove(Move validatedMove) {
        tileSet.playGrabTilesMove(validatedMove);
        getCurrentPlayerRack().addTiles(validatedMove.getTiles());
        player1Turn = !player1Turn;
        previousMoveOpt = Optional.of(validatedMove);
        return 0; // 0 points for a grab move.
    }

    private Optional<String> isValidPlayWordMove(Move move) {
        // Check that the player has the requisite tiles in their rack.
        if (player1Turn && !player1Rack.hasTiles(move.getTiles())) {
            return Optional.of(format("Player 1 doesn't have required tiles: \"%s\"", move.getTilesAsString()));
        } else if (!player1Turn && !player2Rack.hasTiles(move.getTiles())) {
            return Optional.of(format("Player 2 doesn't have required tiles: \"%s\"", move.getTilesAsString()));
        }
        // Check that the play is valid
        return tileSet.isValidPlayWordMove(move);
    }

    private Optional<String> isValidGrabTilesMove(Move move) {
        // Check that the player's tiles won't exceed the maximum number of tiles in hand.
        if (player1Turn && !player1Rack.canAddTiles(move.getTiles())) {
            return Optional.of(format("Player 1 cannot add tiles \"%s\", would exceed max rack size of %d",
                    move.getTilesAsString(),
                    Rack.MAX_TILES_IN_RACK));
        } else if (!player1Turn && !player2Rack.canAddTiles(move.getTiles())) {
            return Optional.of(format("Player 2 cannot add tiles \"%s\", would exceed max rack size of %d",
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