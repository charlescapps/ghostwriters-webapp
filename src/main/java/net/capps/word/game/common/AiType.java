package net.capps.word.game.common;

import net.capps.word.constants.WordConstants;
import net.capps.word.game.ai.BookwormAI;
import net.capps.word.game.ai.GameAI;
import net.capps.word.game.ai.ProfessorAI;
import net.capps.word.game.ai.RandomAI;

/**
 * Created by charlescapps on 2/21/15.
 */
public enum AiType {
    RANDOM_AI(WordConstants.RANDOM_AI_USERNAME),
    BOOKWORM_AI(WordConstants.BOOKWORM_AI_USERNAME),
    PROFESSOR_AI(WordConstants.PROFESSOR_AI_USERNAME);

    private final String systemUsername;

    private AiType(String systemUsername) {
        this.systemUsername = systemUsername;
    }

    public String getSystemUsername() {
        return systemUsername;
    }

    public GameAI getGameAiInstance() {
        switch (this) {
            case RANDOM_AI: return RandomAI.getInstance();
            case BOOKWORM_AI: return BookwormAI.getInstance();
            case PROFESSOR_AI: return ProfessorAI.getInstance();
        }
        throw new IllegalStateException();
    }
}
