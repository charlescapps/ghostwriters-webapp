package net.capps.word.game.tile;

import com.google.common.collect.ImmutableMap;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by charlescapps on 1/25/15.
 */
public class LetterPoints {
    private ImmutableMap<Character, Integer> pointsMap;
    private static final String RESOURCE_FILE = "net/capps/word/points/DefaultPoints.txt";

    private static final LetterPoints INSTANCE = new LetterPoints();

    public static LetterPoints getInstance() {
        return INSTANCE;
    }

    public LetterPoints() { }

    public void load() throws Exception {
        ImmutableMap.Builder<Character, Integer> builder = ImmutableMap.builder();
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(RESOURCE_FILE)) {
            Properties properties = new Properties();
            properties.load(is);

            for (char c = 'A'; c <= 'Z'; c++) {
                String pointValue = properties.getProperty(Character.toString(c));
                if (pointValue == null) {
                    throw new IllegalStateException("Missing point value for character: " + c);
                }
                int intValue = Integer.parseInt(pointValue);
                builder.put(c, intValue);
            }
        }

        pointsMap = builder.build();
    }

    public int getPointValue(char c) {
        Integer points = pointsMap.get(Character.toUpperCase(c));
        if (points == null) {
            throw new IllegalStateException("Missing point value for character: " + c);
        }
        return points;
    }
}
