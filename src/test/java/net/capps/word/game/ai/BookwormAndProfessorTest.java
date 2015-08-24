package net.capps.word.game.ai;

import net.capps.word.game.board.Game;
import net.capps.word.game.board.SquareSet;
import net.capps.word.game.board.TileSet;
import net.capps.word.game.common.AiType;
import net.capps.word.game.common.BoardSize;
import net.capps.word.game.common.GameDensity;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.gen.DefaultGameGenerator;
import net.capps.word.game.gen.DefaultSquareSetGenerator;
import net.capps.word.game.move.Move;
import net.capps.word.game.move.MoveType;
import net.capps.word.heroku.SetupHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by charlescapps on 8/24/15.
 */
public class BookwormAndProfessorTest {
    private static final SetupHelper setupHelper = SetupHelper.getInstance();
    private static final int NUM_GAMES = 10;

    @BeforeClass
    public static void init() throws Exception {
        setupHelper.initDictionaryDataStructures();
        setupHelper.initGameDataStructures();
    }

    @Test
    public void testMeasureBookwormPerformance() {
        doTestPerformance(AiType.BOOKWORM_AI, BoardSize.VENTI);
    }

    @Test
    public void testMeasureProfessorPerformance() {
        doTestPerformance(AiType.PROFESSOR_AI, BoardSize.VENTI);
    }


    private void doTestPerformance(AiType aiType, BoardSize boardSize) {
        System.out.println("Measuring performance for AI = " + aiType + ", boardSize = " + boardSize);
        List<Long> playMoveTimes = new ArrayList<>();
        List<Long> grabMoveTimes = new ArrayList<>();

        for (int i = 0; i < NUM_GAMES; ++i) {
            Game game = createNewGame(boardSize, GameDensity.REGULAR);
            GameAI gameAI = aiType.getGameAiInstance(boardSize);

            while (game.getGameResult() == GameResult.IN_PROGRESS) {
                final long START = System.currentTimeMillis();
                Move move = gameAI.getNextMove(game);
                final long DURATION = System.currentTimeMillis() - START;
                if (move.getMoveType() == MoveType.PLAY_WORD) {
                    playMoveTimes.add(DURATION);
                } else if (move.getMoveType() == MoveType.GRAB_TILES) {
                    grabMoveTimes.add(DURATION);
                }
                game.playMove(move);
            }
        }

        System.out.println("PLAY MOVE TIMES:");
        for (Long time: playMoveTimes) {
            System.out.println(time);
        }

        System.out.println("GRAB MOVE TIMES:");
        for (Long time: grabMoveTimes) {
            System.out.println(time);
        }
    }

    private Game createNewGame(BoardSize bs, GameDensity gd) {
        TileSet tileSet = DefaultGameGenerator.getInstance().generateRandomFinishedGame(bs.getN(), gd.getNumWords(bs), bs.getN());
        SquareSet squareSet = DefaultSquareSetGenerator.getInstance().generateRandomBonusLayout(bs);
        return new Game(-1, GameResult.IN_PROGRESS, tileSet, squareSet, "", "", 0, 0, true, Optional.empty());
    }
}
