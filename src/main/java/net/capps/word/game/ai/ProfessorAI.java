package net.capps.word.game.ai;

/**
 * Created by charlescapps on 2/22/15.
 *
 * Similar to BookwormAI, but
 * 1) Always try to Grab tiles first, if possible
 *
 * 2) Grab moves try to get as many tiles as possible considering all start positions.
 *
 * 3) Play moves only return a move from the top 50% of words, by length.
 *
 */
public class ProfessorAI extends BookwormAI {
    private static final ProfessorAI INSTANCE = new ProfessorAI();

    private static final float PROBABILITY_TO_GRAB = 0.9f;
    private static final float FRACTION_OF_POSITIONS_TO_CHECK = 0.5f;

    // Singleton pattern
    private ProfessorAI() {
        super();
    }

    public static ProfessorAI getInstance() {
        return INSTANCE;
    }

    @Override
    public float getFractionOfPositionsToSearch() {
        return FRACTION_OF_POSITIONS_TO_CHECK;
    }

    @Override
    public float getProbabilityToGrab() {
        return PROBABILITY_TO_GRAB;
    }
}
