package net.capps.word.game.common;

import net.capps.word.game.ai.GameAi;
import net.capps.word.game.ai.RandomAi;

/**
 * Created by charlescapps on 2/21/15.
 */
public enum AiType {
    RANDOM_AI, BOOKWORM_AI, PROFESSOR_AI;

    public GameAi getGameAiInstance() {
        switch (this) {
            case RANDOM_AI: return RandomAi.getInstance();
        }
        throw new IllegalStateException();
    }
}
