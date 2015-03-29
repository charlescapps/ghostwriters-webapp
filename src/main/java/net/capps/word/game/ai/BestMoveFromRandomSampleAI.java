package net.capps.word.game.ai;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.capps.word.game.board.GameState;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.Rack;
import net.capps.word.game.dict.Dictionaries;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.dict.DictionaryTrie;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.RackTile;
import net.capps.word.game.tile.Tile;
import net.capps.word.util.RandomUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 2/22/15.
 *
 * Similar to MonkeyAI, but
 * 1) Grab moves try to get as many tiles as possible in both directions from the randomly chosen start tile
 *
 * 2) Play moves only return a move from the top 75% of words, by length.
 *
 */
public class BestMoveFromRandomSampleAI implements GameAI {
    private static final DictionarySet SET = Dictionaries.getAllWordsSet();
    private static final DictionaryTrie TRIE = Dictionaries.getAllWordsTrie();
    private static final GrabTileHelper GRAB_TILE_HELPER = GrabTileHelper.getInstance();

    private final float fractionOfPositionsToSearch;
    private final float probabilityToGrab;

    public BestMoveFromRandomSampleAI(float fractionOfPositionsToSearch, float probabilityToGrab) {
        Preconditions.checkArgument(fractionOfPositionsToSearch > 0 && fractionOfPositionsToSearch <= 1);
        this.fractionOfPositionsToSearch = fractionOfPositionsToSearch;
        this.probabilityToGrab = probabilityToGrab;
    }

    @Override
    public Move getNextMove(GameState gameState) {

        // If rack is smaller than largest possible word (N), attempt a grab tiles move first 75% of the time
        if (gameState.getCurrentPlayerRack().size() < gameState.getN()) {
            final float rand = ThreadLocalRandom.current().nextFloat();
            if (rand < probabilityToGrab) {
                // Try a grab move first, then if no move is found, try playing
                Optional<Move> grabMove = getBestGrabMoveFromSubsetOfPositions(gameState);
                if (grabMove.isPresent()) {
                    return grabMove.get();
                }

                Optional<Move> playMove = getBestMoveFromRandomSubsetOfPositions(gameState, gameState.getCurrentPlayerRack(), gameState.getTileSet());
                if (playMove.isPresent()) {
                    return playMove.get();
                }

                return Move.passMove(gameState.getGameId());
            }
        }

        // Otherwise...
        // Try a play move first, then if no move is found, try grabbing
        Optional<Move> playMove = getBestMoveFromRandomSubsetOfPositions(gameState, gameState.getCurrentPlayerRack(), gameState.getTileSet());
        if (playMove.isPresent()) {
            return playMove.get();
        }

        Optional<Move> grabMove = getBestGrabMoveFromSubsetOfPositions(gameState);
        if (grabMove.isPresent()) {
            return grabMove.get();
        }

        return Move.passMove(gameState.getGameId());
    }

    // --------------- Private ----------------

    private Optional<Move> getBestMoveFromRandomSubsetOfPositions(GameState gameState, Rack rack, TileSet tileSet) {
        if (rack.isEmpty()) {
            return Optional.absent();
        }

        List<Pos> unoccupiedPositions = tileSet.getAllUnoccupiedPositions();
        if (unoccupiedPositions.isEmpty()) {
            return Optional.absent();
        }

        // Search the possible start positions in a random order.
        final List<Pos> randomOrderPositions = RandomUtil.shuffleList(unoccupiedPositions);
        final int numPositionsToCheck = (int)Math.ceil(fractionOfPositionsToSearch * randomOrderPositions.size());

        Move bestMove = null;
        int numChecked = 0;

        for (Pos p: randomOrderPositions) {
            if (numChecked > numPositionsToCheck && bestMove != null) {
                break;
            }
            Dir[] randomOrderDirs = RandomUtil.shuffleArray(Dir.VALID_PLAY_DIRS);
            for (Dir dir: randomOrderDirs) {
                Optional<Move> bestMoveForPositionOpt = getBestMoveFromStartPos(gameState, tileSet, rack, p, dir);
                if (bestMoveForPositionOpt.isPresent()) {
                    if (bestMove == null || bestMoveForPositionOpt.get().getPoints() > bestMove.getPoints()) {
                        bestMove = bestMoveForPositionOpt.get();
                    }
                }
            }
            ++numChecked;
        }

        return Optional.fromNullable(bestMove);
    }

    private Optional<Move> getBestGrabMoveFromSubsetOfPositions(GameState gameState) {
        final TileSet tileSet = gameState.getTileSet();

        int maxToGrab = gameState.isPlayer1Turn() ?
                Rack.MAX_TILES_IN_RACK - gameState.getPlayer1Rack().size() :
                Rack.MAX_TILES_IN_RACK - gameState.getPlayer2Rack().size();
        maxToGrab = Math.min(maxToGrab, tileSet.N);

        if (maxToGrab <= 0) {
            return Optional.absent();
        }

        final List<Pos> startPosList = tileSet.getAllStartTilePositions();

        if (startPosList.isEmpty()) {
            return Optional.absent();
        }

        List<Pos> startPosListRandom = RandomUtil.shuffleList(startPosList);
        final int numToCheck = (int)Math.ceil(fractionOfPositionsToSearch * startPosListRandom.size());

        int bestPointValue = 0;
        Move bestMove = null;

        for (int i = 0; i < numToCheck; i++) {
            Pos start = startPosListRandom.get(i);
            Move grabMove = GRAB_TILE_HELPER.getLongestGrabMove(gameState, start, maxToGrab);
            int pointValueOfLetters = grabMove.computeSumOfTilePoints();
            if (bestMove == null || pointValueOfLetters > bestPointValue) {
                bestMove = grabMove;
                bestPointValue = pointValueOfLetters;
            }
        }

        return Optional.of(bestMove); // Should be impossible for bestMove to be null.
    }

    private Move getGrabMoveFromStartPos(Pos start, GameState gameState, int maxToGrab) {
        List<Dir> occupiedDirs = Lists.newArrayList();
        final TileSet tileSet = gameState.getTileSet();

        // Find the directions that have occupied tiles
        for (Pos p: start.adjacents()) {
            if (tileSet.isValid(p) && tileSet.get(p).isStartTile()) {
                occupiedDirs.add(start.getDirTo(p));
            }
        }

        // If no adjacent tiles are occupied, just grab a single tile
        if (occupiedDirs.isEmpty()) {
            char letter = tileSet.getLetterAt(start);
            String letters = Character.toString(letter);
            return new Move(gameState.getGameId(), MoveType.GRAB_TILES, letters, start, Dir.E, Lists.newArrayList(RackTile.of(letter)));
        }

        // Else grab all tiles in BOTH directions
        int dirIndex = ThreadLocalRandom.current().nextInt(occupiedDirs.size());
        final Dir dir = occupiedDirs.get(dirIndex);

        // Get the grab start position by finding the first occupied tile
        Dir reverseDir = dir.negate();
        final Pos grabStart = tileSet.getEndOfStartTiles(start.go(reverseDir), reverseDir);

        StringBuilder sb = new StringBuilder();
        List<RackTile> grabbedTiles = Lists.newArrayList();

        for (Pos p = grabStart; tileSet.isValid(p) && tileSet.get(p).isStartTile(); p = p.go(dir)) {
            Tile tile = tileSet.get(p);
            sb.append(tile.getLetter());
            grabbedTiles.add(tile.toRackTile());
            if (sb.length() >= maxToGrab) {
                break;
            }
        }

        String letters = sb.toString();
        return new Move(gameState.getGameId(), MoveType.GRAB_TILES, letters, grabStart, dir, grabbedTiles);
    }

    private Optional<Move> getBestMoveFromStartPos(GameState gameState, TileSet tileSet, Rack rack, Pos start, Dir dir) {
        // Precondition: the start pos isn't an occupied tile.
        final Pos originalStart = start;
        Optional<Pos> firstOccupiedOrAdjacent = tileSet.getFirstOccupiedOrAdjacent(start, dir, rack.size());

        if (!firstOccupiedOrAdjacent.isPresent()) {
            firstOccupiedOrAdjacent = Optional.of(start); // Try playing a word off in space
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

        generateMoves(gameState.getGameId(), prefix, diff + 1, tileSet, start, originalStart, dir, placements, rackCopy, foundMoves);

        // If no moves are found, return Optional.absent()
        if (foundMoves.isEmpty()) {
            return Optional.absent();
        }

        int bestScore = 0;
        Move bestMove = null;
        for (Move move: foundMoves) {
            int score = gameState.computePoints(move);
            move.setPoints(score);
            if (score > bestScore || bestMove == null) {
                bestScore = score;
                bestMove = move;
            }
        }

        return Optional.of(bestMove);
    }

    private void generateMoves(int gameId, String prefix, int minPlacements, TileSet tileSet, Pos start, Pos tryPos, Dir dir, List<RackTile> placements, List<RackTile> remaining, List<Move> moves) {

        if (!TRIE.isPrefix(prefix)) {
            return;
        }

        if (placements.size() >= minPlacements && SET.contains(prefix)) {
            List<RackTile> usedTiles = ImmutableList.<RackTile>builder().addAll(placements).build();
            Move move = new Move(gameId, MoveType.PLAY_WORD, prefix, start, dir, usedTiles);
            if (!tileSet.getPlayWordMoveError(move).isPresent()) {
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
