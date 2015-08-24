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

    public float getBoardSearchFraction(BoardSize boardSize) {
        switch (this) {
            case RANDOM_AI:
                return 0f;
            case BOOKWORM_AI:
                switch (boardSize) {
                    case TALL: return 0.95f;
                    case GRANDE: return 0.5f;
                    case VENTI: return 0.3f;
                }
            case PROFESSOR_AI:
                switch (boardSize) {
                    case TALL: return 1f;
                    case GRANDE: return 0.75f;
                    case VENTI: return 0.5f;
                }
        }
        throw new IllegalStateException();
    }

    public GameAI getGameAiInstance(BoardSize boardSize) {
        switch (this) {
            case RANDOM_AI: return RandomAI.getInstance();
            case BOOKWORM_AI: return BookwormAI.getInstance(boardSize);
            case PROFESSOR_AI: return ProfessorAI.getInstance(boardSize);
        }
        throw new IllegalStateException();
    }
}
