package net.capps.word.game.ai;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.capps.word.game.board.GameState;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.Rack;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.dict.DictionaryTrie;
import net.capps.word.game.gen.PositionLists;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.RackTile;
import net.capps.word.game.tile.Tile;
import net.capps.word.util.RandomUtil;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 2/22/15.
 */
public class RandomAi implements GameAi {
    private static final PositionLists POSITION_LISTS = PositionLists.getInstance();
    private static final DictionaryTrie TRIE = DictionaryTrie.getInstance();
    private static final DictionarySet SET = DictionarySet.getInstance();

    private static final RandomAi INSTANCE = new RandomAi();

    public static RandomAi getInstance() {
        return INSTANCE;
    }

    private RandomAi() { } // Singleton pattern.

    @Override
    public Move getNextMove(GameState gameState) {

        Optional<Move> playMove = getRandomPlayMove(gameState.getGameId(), gameState.getCurrentPlayerRack(), gameState.getTileSet());
        if (playMove.isPresent()) {
            return playMove.get();
        }

        Optional<Move> grabMove = getRandomGrabMove(gameState, gameState.getTileSet());
        if (grabMove.isPresent()) {
            return grabMove.get();
        }

        return Move.passMove(gameState.getGameId());
    }

    private Optional<Move> getRandomPlayMove(int gameId, Rack rack, TileSet tileSet) {
        if (rack.isEmpty()) {
            return Optional.absent();
        }
        final int N = tileSet.N;

        List<Pos> positions = POSITION_LISTS.getPositionList(N);

        // Search the possible start positions in a random order.
        List<Pos> randomOrderPositions = RandomUtil.shuffleList(positions);

        for (Pos p: randomOrderPositions) {
            if (!tileSet.isOccupied(p)) {
                Dir[] randomOrderDirs = RandomUtil.shuffleArray(Dir.VALID_PLAY_DIRS);
                for (Dir dir: randomOrderDirs) {
                    Optional<Move> optValidMove = getFirstValidMoveFromUnoccupiedStartTile(gameId, tileSet, rack, p, dir);
                    if (optValidMove.isPresent()) {
                        return optValidMove;
                    }
                }
            }
        }

        return Optional.absent();
    }

    private Optional<Move> getRandomGrabMove(GameState gameState, TileSet tileSet) {
        final int gameId = gameState.getGameId();
        final int maxToGrab = gameState.isPlayer1Turn() ?
                Rack.MAX_TILES_IN_RACK - gameState.getPlayer1Rack().size() :
                Rack.MAX_TILES_IN_RACK - gameState.getPlayer2Rack().size();

        if (maxToGrab <= 0) {
            return Optional.absent();
        }

        final List<Pos> startPosList = tileSet.getAllStartTilePositions();

        if (startPosList.isEmpty()) {
            return Optional.absent();
        }
        final Random random = ThreadLocalRandom.current();
        int index = random.nextInt(startPosList.size());
        final Pos start = startPosList.get(index);

        List<Dir> occupiedDirs = Lists.newArrayList();

        // Find the directions that have occupied tiles
        for (Pos p: start.adjacents()) {
            if (tileSet.isOccupied(p) && tileSet.get(p).isStartTile()) {
                occupiedDirs.add(start.getDirTo(p));
            }
        }

        // If no adjacent tiles are occupied, just grab a single tile
        if (occupiedDirs.isEmpty()) {
            char letter = tileSet.getLetterAt(start);
            String letters = Character.toString(letter);
            Move move = new Move(gameId, MoveType.GRAB_TILES, letters, start, Dir.E, Lists.newArrayList(RackTile.of(letter)));
            return Optional.of(move);
        }

        // Else grab all tiles in one direction
        int dirIndex = random.nextInt(occupiedDirs.size());
        final Dir dir = occupiedDirs.get(dirIndex);

        StringBuilder sb = new StringBuilder();
        List<RackTile> grabbedTiles = Lists.newArrayList();

        for (Pos p = start; tileSet.isOccupied(p) && tileSet.get(p).isStartTile(); p = p.go(dir)) {
            Tile tile = tileSet.get(p);
            sb.append(tile.getLetter());
            grabbedTiles.add(tile.toRackTile());
            if (sb.length() >= maxToGrab) {
                break;
            }
        }

        String letters = sb.toString();
        Move move = new Move(gameId, MoveType.GRAB_TILES, letters, start, dir, grabbedTiles);
        return Optional.of(move);
    }

    private Optional<Move> getFirstValidMoveFromUnoccupiedStartTile(int gameId, TileSet tileSet, Rack rack, Pos start, Dir dir) {
        // Precondition: the start pos isn't an occupied tile.
        final Pos originalStart = start;
        Optional<Pos> firstOccupiedOrAdjacent = tileSet.getFirstOccupiedOrAdjacent(start, dir, rack.size());

        if (!firstOccupiedOrAdjacent.isPresent()) {
            return Optional.absent();
        }

        Pos occOrAdj = firstOccupiedOrAdjacent.get();

        // If the tile in the reverse direction is occupied, we must consider our play including all occupied tiles
        // in that direction.
        Pos previous = start.go(dir.negate());
        String prefix = "";

        // Compute the prefix if present.
        if (tileSet.isOccupied(previous)) {
            start = tileSet.getEndOfOccupied(previous, dir.negate());
            StringBuilder sb = new StringBuilder();
            for (Pos p = start; !p.equals(originalStart); p = p.go(dir)) {
                sb.append(tileSet.getLetterAt(p));
            }
            prefix = sb.toString();
        }

        List<RackTile> rackCopy = rack.getRackCopy();
        List<RackTile> placements = Lists.newArrayList();
        List<Move> foundMoves = Lists.newArrayList();

        final int diff = occOrAdj.minus(start);

        generateMoves(gameId, prefix, diff + 1, tileSet, start, originalStart, dir, placements, rackCopy, foundMoves);

        // If no moves are found, return Optional.absent()
        if (foundMoves.isEmpty()) {
            return Optional.absent();
        }
        // If we find moves, return a random move starting at this position
        Random random = ThreadLocalRandom.current();
        int choice = random.nextInt(foundMoves.size());
        return Optional.of(foundMoves.get(choice));

    }

    private void generateMoves(int gameId, String prefix, int minPlacements, TileSet tileSet, Pos start, Pos tryPos, Dir dir, List<RackTile> placements, List<RackTile> remaining, List<Move> moves) {

        if (!TRIE.isPrefix(prefix)) {
            return;
        }

        if (placements.size() >= minPlacements && SET.contains(prefix)) {
            List<RackTile> usedTiles = ImmutableList.<RackTile>builder().addAll(placements).build();
            Move move = new Move(gameId, MoveType.PLAY_WORD, prefix, start, dir, usedTiles);
            if (!tileSet.isValidPlayWordMove(move).isPresent()) {
                moves.add(move);
            }
        }

        Pos nextPos = tryPos.go(dir);
        if (!tileSet.isValid(nextPos)) {
            return;
        }

        if (tileSet.isOccupied(tryPos)) {
            prefix += tileSet.getLetterAt(tryPos);
            generateMoves(gameId, prefix, minPlacements, tileSet, start, nextPos, dir, placements, remaining, moves);
        } else {
            Set<RackTile> remainingSet = Sets.newHashSet(remaining);
            for (RackTile rackTile : remainingSet) {
                String word = prefix + rackTile.getLetter();
                List<RackTile> newPlacements = Lists.newArrayList(placements);
                List<RackTile> newRemaining = Lists.newArrayList(remaining);
                newPlacements.add(rackTile);
                newRemaining.remove(rackTile);
                generateMoves(gameId, word, minPlacements, tileSet, start, nextPos, dir, newPlacements, newRemaining, moves);
            }
        }
    }

}
