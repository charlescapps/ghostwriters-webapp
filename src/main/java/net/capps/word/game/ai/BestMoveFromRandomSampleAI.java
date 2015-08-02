package net.capps.word.game.ai;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.capps.word.game.board.Game;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.common.Rack;
import net.capps.word.game.dict.DictType;
import net.capps.word.game.dict.common.DictHelpers;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.RackTile;
import net.capps.word.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private static final GrabTileHelper GRAB_TILE_HELPER = GrabTileHelper.getInstance();

    private final float fractionOfPositionsToSearch;
    private final float probabilityToGrab;
    private final float probabilityToSelectWordFromSpecialDict;

    public BestMoveFromRandomSampleAI(float fractionOfPositionsToSearch, float probabilityToGrab, float probabilityToSelectWordFromSpecialDict) {
        Preconditions.checkArgument(fractionOfPositionsToSearch > 0 && fractionOfPositionsToSearch <= 1.f);
        this.fractionOfPositionsToSearch = fractionOfPositionsToSearch;
        this.probabilityToGrab = probabilityToGrab;
        this.probabilityToSelectWordFromSpecialDict = probabilityToSelectWordFromSpecialDict;
    }

    @Override
    public Move getNextMove(Game game) {

        // If rack is smaller than largest possible word (N), attempt a grab tiles move first 75% of the time
        if (game.getCurrentPlayerRack().size() < game.getN()) {
            final float rand = ThreadLocalRandom.current().nextFloat();
            if (rand < probabilityToGrab) {
                // Try a grab move first, then if no move is found, try playing
                Optional<Move> grabMove = getBestGrabMoveFromSubsetOfPositions(game);
                if (grabMove.isPresent()) {
                    return grabMove.get();
                }

                Optional<Move> playMove = getBestMoveFromRandomSubsetOfPositions(game, game.getCurrentPlayerRack(), game.getTileSet());
                if (playMove.isPresent()) {
                    return playMove.get();
                }

                return Move.passMove(game.getGameId());
            }
        }

        // Otherwise...
        // Try a play move first, then if no move is found, try grabbing
        Optional<Move> playMove = getBestMoveFromRandomSubsetOfPositions(game, game.getCurrentPlayerRack(), game.getTileSet());
        if (playMove.isPresent()) {
            return playMove.get();
        }

        Optional<Move> grabMove = getBestGrabMoveFromSubsetOfPositions(game);
        if (grabMove.isPresent()) {
            return grabMove.get();
        }

        return Move.passMove(game.getGameId());
    }

    // --------------- Private ----------------

    private Optional<Move> getBestMoveFromRandomSubsetOfPositions(Game game, Rack rack, TileSet tileSet) {
        if (!rack.hasLetterTile()) {
            return Optional.empty();
        }

        List<Pos> unoccupiedPositions = tileSet.getAllUnoccupiedPositions();
        if (unoccupiedPositions.isEmpty()) {
            return Optional.empty();
        }

        final DictType[] dictionaryOrder = DictHelpers.selectDictionaryOrderForMove(game.getSpecialDict(), probabilityToSelectWordFromSpecialDict);

        // Search the possible start positions in a random order.
        final List<Pos> randomOrderPositions = RandomUtil.shuffleList(unoccupiedPositions);
        final int numPositionsToCheck = (int)Math.ceil(fractionOfPositionsToSearch * randomOrderPositions.size());

        for (DictType dictType: dictionaryOrder) {
            Optional<Move> moveOptional = getBestMoveForDictType(dictType, game, rack, tileSet, randomOrderPositions, numPositionsToCheck);
            if (moveOptional.isPresent()) {
                return moveOptional;
            }
        }
        return Optional.empty();
    }

    private Optional<Move> getBestMoveForDictType(DictType dictType, Game game, Rack rack, TileSet tileSet, List<Pos> randomOrderPositions, int numPositionsToCheck) {
        Move bestMove = null;
        int numChecked = 0;

        for (Pos p: randomOrderPositions) {
            if (numChecked > numPositionsToCheck && bestMove != null) {
                break;
            }
            Dir[] randomOrderDirs = RandomUtil.shuffleArray(Dir.VALID_PLAY_DIRS);
            for (Dir dir: randomOrderDirs) {
                Optional<Move> bestMoveForPositionOpt = getBestMoveFromStartPos(game, dictType, tileSet, rack, p, dir);
                if (bestMoveForPositionOpt.isPresent()) {
                    if (bestMove == null || bestMoveForPositionOpt.get().getPoints() > bestMove.getPoints()) {
                        bestMove = bestMoveForPositionOpt.get();
                    }
                }
            }
            ++numChecked;
        }

        return Optional.ofNullable(bestMove);
    }

    private Optional<Move> getBestGrabMoveFromSubsetOfPositions(Game game) {
        final TileSet tileSet = game.getTileSet();

        int maxToGrab = game.isPlayer1Turn() ?
                Rack.MAX_TILES_IN_RACK - game.getPlayer1Rack().size() :
                Rack.MAX_TILES_IN_RACK - game.getPlayer2Rack().size();
        maxToGrab = Math.min(maxToGrab, tileSet.N);

        if (maxToGrab <= 0) {
            return Optional.empty();
        }

        final List<Pos> startPosList = tileSet.getAllStartTilePositions();

        if (startPosList.isEmpty()) {
            return Optional.empty();
        }

        List<Pos> startPosListRandom = RandomUtil.shuffleList(startPosList);
        final int numToCheck = (int)Math.ceil(fractionOfPositionsToSearch * startPosListRandom.size());

        int bestPointValue = 0;
        Move bestMove = null;

        for (int i = 0; i < numToCheck; i++) {
            Pos start = startPosListRandom.get(i);
            Move grabMove = GRAB_TILE_HELPER.getLongestGrabMove(game, start, maxToGrab);
            int pointValueOfLetters = grabMove.computeSumOfTilePoints();
            if (bestMove == null || pointValueOfLetters > bestPointValue) {
                bestMove = grabMove;
                bestPointValue = pointValueOfLetters;
            }
        }

        return Optional.of(bestMove); // Should be impossible for bestMove to be null.
    }

    private Optional<Move> getBestMoveFromStartPos(Game game, DictType dictType, TileSet tileSet, Rack rack, Pos start, Dir dir) {
        // Precondition: the start pos isn't an occupied tile.
        final Pos originalStart = start;
        Optional<Pos> firstOccupiedOrAdjacent = tileSet.getFirstOccupiedOrAdjacent(start, dir, rack.getNumLetterTiles());

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

        List<RackTile> rackCopy = rack.getLetterTiles();
        List<RackTile> placements = new ArrayList<>();
        List<Move> foundMoves = new ArrayList<>();

        final int diff = occOrAdj.minus(originalStart);

        generateMoves(game.getGameId(), dictType, prefix, diff + 1, tileSet, start, originalStart, dir, placements, rackCopy, foundMoves);

        // If no moves are found, return Optional.empty()
        if (foundMoves.isEmpty()) {
            return Optional.empty();
        }

        int bestScore = 0;
        Move bestMove = null;
        for (Move move: foundMoves) {
            int score = game.computePoints(move);
            move.setPoints(score);
            if (score > bestScore || bestMove == null) {
                bestScore = score;
                bestMove = move;
            }
        }

        return Optional.of(bestMove);
    }

    private void generateMoves(int gameId, DictType dictType, String prefix, int minPlacements, TileSet tileSet, Pos start, Pos tryPos, Dir dir, List<RackTile> placements, List<RackTile> remaining, List<Move> moves) {

        if (!DictHelpers.isPrefix(prefix, dictType)) {
            return;
        }

        if (placements.size() >= minPlacements && DictHelpers.isWord(prefix, dictType)) {
            List<RackTile> usedTiles = ImmutableList.<RackTile>builder().addAll(placements).build();
            Move move = new Move(gameId, MoveType.PLAY_WORD, prefix, start, dir, usedTiles);
            if (!tileSet.getPlayWordMoveError(move, null).isPresent()) {
                moves.add(move);
            }
        }

        Pos nextPos = tryPos.go(dir);
        if (!tileSet.isValid(nextPos)) {
            return;
        }

        if (tileSet.isOccupied(tryPos)) {
            prefix += tileSet.getLetterAt(tryPos);
            generateMoves(gameId, dictType, prefix, minPlacements, tileSet, start, nextPos, dir, placements, remaining, moves);
        } else {
            Set<RackTile> remainingSet = Sets.newHashSet(remaining);
            for (RackTile rackTile : remainingSet) {
                String word = prefix + rackTile.getLetter();
                List<RackTile> newPlacements = Lists.newArrayList(placements);
                List<RackTile> newRemaining = Lists.newArrayList(remaining);
                newPlacements.add(rackTile);
                newRemaining.remove(rackTile);
                generateMoves(gameId, dictType, word, minPlacements, tileSet, start, nextPos, dir, newPlacements, newRemaining, moves);
            }
        }
    }


}
