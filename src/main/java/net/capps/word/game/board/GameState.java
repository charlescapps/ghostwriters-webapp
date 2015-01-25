package net.capps.word.game.board;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.capps.word.exceptions.InvalidBoardException;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.common.Rack;
import net.capps.word.game.move.Move;
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


    public GameState(GameModel gameModel) throws Exception {
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
        switch (move.getMoveType()) {
            case PLAY_WORD: return isValidPlayWordMove(move);
            case GRAB_TILES: return isValidGrabTilesMove(move);
        }
        throw new IllegalStateException();
    }

    public int playMove(Move validatedMove) {
        switch (validatedMove.getMoveType()) {
            case PLAY_WORD:
                return playWordMove(validatedMove);
            case GRAB_TILES:
                return playGrabTilesMove(validatedMove);
            default:
                throw new IllegalStateException();
        }
    }

    private int playWordMove(Move validatedMove) {
        tileSet.playWordMove(validatedMove);
        int numPoints = squareSet.computePoints(validatedMove);
        getCurrentPlayerRack().removeTiles(validatedMove.getTiles());
        if (player1Turn) {
            player1Points += numPoints;
        } else {
            player2Points += numPoints;
        }
        gameResult = checkForGameEnd();
        player1Turn = !player1Turn;
        return numPoints;
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
}
