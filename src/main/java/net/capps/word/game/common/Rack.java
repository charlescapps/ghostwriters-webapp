package net.capps.word.game.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.capps.word.game.tile.RackTile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

/**
 * Created by charlescapps on 1/24/15.
 */
public class Rack implements Iterable<RackTile> {
    private final List<RackTile> tiles;

    public static final int MAX_TILES_IN_RACK = 20;

    public Rack(String letters) {
        tiles = lettersToTiles(letters);
        if (tiles.size() > MAX_TILES_IN_RACK) {
            throw new IllegalArgumentException(
                    format("Cannot have more than %d tiles in rack. Invalid rack: %s", MAX_TILES_IN_RACK, letters));
        }
    }

    public static List<RackTile> lettersToTiles(String letters) {
        Preconditions.checkNotNull(letters);
        List<RackTile> tiles = new ArrayList<>(letters.length());
        for (int i = 0; i < letters.length(); ++i) {
            char c = letters.charAt(i);
            RackTile tile = RackTile.of(c);
            tiles.add(tile);
        }
        return tiles;
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    public int getSumOfPoints() {
        int points = 0;
        for (RackTile rackTile: tiles) {
            points += rackTile.getLetterPointValue();
        }
        return points;
    }

    public List<RackTile> getRackCopy() {
        return Lists.newArrayList(tiles);
    }

    public List<RackTile> getLetterTiles() {
        List<RackTile> letterTiles = new ArrayList<>();
        for (RackTile tile: tiles) {
            if (tile.isLetter()) {
                letterTiles.add(tile);
            }
        }
        return letterTiles;
    }

    public boolean hasTiles(List<RackTile> tilesToPlay) {
        List<RackTile> copyTilesToPlay = Lists.newArrayList(tilesToPlay);
        List<RackTile> copyTiles = Lists.newArrayList(tiles);

        while (!copyTilesToPlay.isEmpty()) {
            RackTile first = copyTilesToPlay.get(0);
            if (!copyTiles.contains(first)) {
                return false;
            }
            copyTilesToPlay.remove(first);
            copyTiles.remove(first);
        }
        return true;
    }

    public void addTiles(List<RackTile> tilesToAdd) {
        Preconditions.checkArgument(canAddTiles(tilesToAdd),
                format("Cannot add tiles, because rack would exceed %d tiles", MAX_TILES_IN_RACK));
        tiles.addAll(tilesToAdd);
    }

    public void removeTiles(List<RackTile> tilesToRemove) {
        if (!hasTiles(tilesToRemove)) {
            throw new IllegalArgumentException(format("Cannot remove tiles \"%s\" from Rack \"%s\"",
                    tilesToString(tilesToRemove), tilesToString(tiles)));
        }
        for (RackTile tileToRemove: tilesToRemove) {
            tiles.remove(tileToRemove);
        }
    }

    public int getNumLetterTiles() {
        int num = 0;
        for (RackTile tile: tiles) {
            if (tile.isLetter()) {
                ++num;
            }
        }
        return num;
    }

    public int size() {
        return tiles.size();
    }

    public boolean canAddTiles(List<RackTile> tilesToAdd) {
        return tiles.size() + tilesToAdd.size() <= MAX_TILES_IN_RACK;
    }

    @Override
    public Iterator<RackTile> iterator() {
        return tiles.iterator();
    }

    @Override
    public String toString() {
        return tilesToString(tiles);
    }

    public static String tilesToString(List<RackTile> rackTiles) {
        StringBuilder sb = new StringBuilder();
        for (RackTile rackTile: rackTiles) {
            sb.append(rackTile.toString());
        }
        return sb.toString();
    }


}
