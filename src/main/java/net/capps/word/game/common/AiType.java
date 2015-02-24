package net.capps.word.game.common;

import net.capps.word.constants.WordConstants;
import net.capps.word.game.ai.GameAi;
import net.capps.word.game.ai.RandomAi;

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

    public GameAi getGameAiInstance() {
        switch (this) {
            case RANDOM_AI: return RandomAi.getInstance();
        }
        throw new IllegalStateException();
    }
}
