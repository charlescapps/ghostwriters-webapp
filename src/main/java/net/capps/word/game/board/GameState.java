package net.capps.word.game.board;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.capps.word.exceptions.InvalidBoardException;
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
    private final int N;
    private final SquareSet squareSet;
    private final TileSet tileSet;
    private Rack player1Rack;
    private Rack player2Rack;
    private boolean player1Turn;


    public GameState(GameModel gameModel) throws Exception {
        Preconditions.checkNotNull(gameModel);
        Preconditions.checkNotNull(gameModel.getBoardSize());
        Preconditions.checkNotNull(gameModel.getTiles());
        Preconditions.checkNotNull(gameModel.getSquares());
        Reader squareReader = new StringReader(gameModel.getSquares());
        Reader tileReader = new StringReader(gameModel.getTiles());
        N = gameModel.getBoardSize().getN();
        squareSet = new SquareSet(N);
        tileSet = new TileSet(N);
        load(squareReader, tileReader);
        player1Rack = new Rack(gameModel.getPlayer1Rack());
        player2Rack = new Rack(gameModel.getPlayer2Rack());
        player1Turn = gameModel.getPlayer1Turn();
    }

    public void load(Reader squareReader, Reader tileReader) throws IOException, InvalidBoardException {
        squareSet.load(squareReader);
        tileSet.load(tileReader);
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

    public boolean isPlayer1Turn() {
        return player1Turn;
    }

    public Rack getCurrentPlayerRack() {
        return player1Turn ? player1Rack : player2Rack;
    }

    public Optional<String> isValidMove(Move move) {
        switch (move.getMoveType()) {
            case PLAY_WORD: return isValidPlayWordMove(move);
            case GRAB_TILES: return isValidGrabTilesMove(move);
        }
        throw new IllegalStateException();
    }

    public void playMove(Move validatedMove) {
        switch (validatedMove.getMoveType()) {
            case PLAY_WORD:
                playWordMove(validatedMove);
                break;
            case GRAB_TILES:
                playGrabTilesMove(validatedMove);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void playWordMove(Move move) {
        tileSet.playWordMove(move);
        getCurrentPlayerRack().removeTiles(move.getTiles());
        player1Turn = !player1Turn;
    }

    private void playGrabTilesMove(Move move) {
        tileSet.playGrabTilesMove(move);
        getCurrentPlayerRack().addTiles(move.getTiles());
        player1Turn = !player1Turn;
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
