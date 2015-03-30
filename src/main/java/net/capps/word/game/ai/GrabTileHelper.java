package net.capps.word.game.ai;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.capps.word.game.board.Game;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.Dir;
import net.capps.word.game.common.Pos;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.game.tile.RackTile;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 3/28/15.
 */
public class GrabTileHelper {
    private static final GrabTileHelper INSTANCE = new GrabTileHelper();

    public static GrabTileHelper getInstance() {
        return INSTANCE;
    }

    private GrabTileHelper() {
    }

    public Move getLongestGrabMove(Game game, Pos start, int maxToGrab) {
        Preconditions.checkArgument(maxToGrab > 0, "Can't get a grab move when maxToGrab isn't positive!");
        final TileSet tileSet = game.getTileSet();

        final List<Dir> occupiedDirs = Lists.newArrayListWithCapacity(4);

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
            return new Move(game.getGameId(), MoveType.GRAB_TILES, letters, start, Dir.E, Lists.newArrayList(RackTile.of(letter)));
        }

        // Else, try to grab as many tiles as possible starting from the start position, then expanding backwards
        int dirIndex = ThreadLocalRandom.current().nextInt(occupiedDirs.size());
        final Dir dir = occupiedDirs.get(dirIndex);

        final Dir reverseDir = dir.negate();
        Pos grabStart, grabEnd;

        for (grabEnd = start; tileSet.isValid(grabEnd) && tileSet.get(grabEnd).isStartTile(); grabEnd = grabEnd.go(dir)) {
            int numGrabbed = grabEnd.minus(start) + 1;
            if (numGrabbed > maxToGrab) {
                break;
            }
        }
        grabEnd = grabEnd.go(reverseDir);

        for (grabStart = start; tileSet.isValid(grabStart) && tileSet.get(grabStart).isStartTile(); grabStart = grabStart.go(reverseDir)) {
            int numGrabbed = grabEnd.minus(grabStart) + 1;
            if (numGrabbed > maxToGrab) {
                break;
            }
        }
        grabStart = grabStart.go(dir);

        StringBuilder sb = new StringBuilder();
        List<RackTile> grabbedTiles = Lists.newArrayList();
        final Pos afterEnd = grabEnd.go(dir);
        for (Pos p = grabStart; !p.equals(afterEnd); p = p.go(dir)) {
            final char c = tileSet.getLetterAt(p);
            grabbedTiles.add(RackTile.of(c));
            sb.append(c);
        }

        String letters = sb.toString();
        return new Move(game.getGameId(), MoveType.GRAB_TILES, letters, grabStart, dir, grabbedTiles);
    }
}
