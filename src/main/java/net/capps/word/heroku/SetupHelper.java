package net.capps.word.heroku;

import net.capps.word.constants.WordConstants;
import net.capps.word.db.TableDefinitions;
import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.board.FixedLayouts;
import net.capps.word.game.dict.Dictionaries;
import net.capps.word.game.gen.PositionLists;
import net.capps.word.game.tile.LetterPoints;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

/**
 * Created by charlescapps on 12/28/14.
 */
public class SetupHelper {

    private static final SetupHelper INSTANCE = new SetupHelper();
    private static final UsersProvider usersProvider = UsersProvider.getInstance();
    private static final UsersDAO usersDAO = UsersDAO.getInstance();

    private SetupHelper() {}

    public static SetupHelper getInstance() {
        return INSTANCE;
    }

    public void initDatabase() throws Exception {
        WordDbManager wordDbManager = WordDbManager.getInstance();

        try(Connection connection = wordDbManager.getConnection()) {
            Statement stmt = connection.createStatement();

            // --------- word_users table ----------
            stmt.executeUpdate(TableDefinitions.CREATE_WORD_USERS_TABLE);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.DROP_LOWERCASE_USER_IDX);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_LOWERCASE_USER_IDX);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.DROP_USER_RATING_IDX);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_USER_RATING_IDX);

            // Create user ranking view.
            stmt = connection.createStatement();
            stmt.executeUpdate(UsersDAO.CREATE_RANKING_VIEW);

            // -------- word_games table --------
            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_GAMES_TABLE);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.DROP_PLAYER1_IDX);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_PLAYER1_IDX);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.DROP_PLAYER2_IDX);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_PLAYER2_IDX);

            // -------- word_moves table -------
            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_MOVES_TABLE);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.DROP_GAME_ID_FOR_MOVES_TABLE_IDX);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_GAME_ID_FOR_MOVES_TABLE_IDX);

            // -------- word_sessions table

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_SESSION_TABLE);

            // --------- played_words table ------
            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_PLAYED_WORDS_TABLE);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.DROP_PLAYED_WORDS_USER_ID_IDX);

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_PLAYED_WORDS_USER_ID_IDX);

        }
    }

    public void createAiUsers() throws Exception {
        UserModel randomUser = new UserModel(null, WordConstants.RANDOM_AI_USERNAME, null, null, null, true);
        UserModel bookwormUser = new UserModel(null, WordConstants.BOOKWORM_AI_USERNAME, null, null, null, true);
        UserModel professorUser = new UserModel(null, WordConstants.PROFESSOR_AI_USERNAME, null, null, null, true);

        usersProvider.createNewUserIfNotExists(randomUser);
        usersProvider.createNewUserIfNotExists(bookwormUser);
        usersProvider.createNewUserIfNotExists(professorUser);

        Optional<UserModel> createdRandomUser = usersDAO.getUserByUsername(WordConstants.RANDOM_AI_USERNAME, true);
        Optional<UserModel> createdBookwormUser = usersDAO.getUserByUsername(WordConstants.BOOKWORM_AI_USERNAME, true);
        Optional<UserModel> createdProfessorUser = usersDAO.getUserByUsername(WordConstants.PROFESSOR_AI_USERNAME, true);

        // They must be present since we just inserted them into the Database!
        WordConstants.RANDOM_AI_USER.set(createdRandomUser.get());
        WordConstants.BOOKWORM_AI_USER.set(createdBookwormUser.get());
        WordConstants.PROFESSOR_AI_USER.set(createdProfessorUser.get());
    }

    public void initGameDataStructures() throws Exception {
        FixedLayouts.getInstance().initLayouts();
        LetterPoints.getInstance().load();
        PositionLists.getInstance().load();
    }

    public void initDictionaryDataStructures() throws IOException {
        Dictionaries.initializeAllDictionaries();
    }

    public void initJetty() throws Exception {

        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        final Server server = new Server(Integer.valueOf(webPort));
        final WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        // Parent loader priority is a class loader setting that Jetty accepts.
        // By default Jetty will behave like most web containers in that it will
        // allow your application to replace non-server libraries that are part of the
        // container. Setting parent loader priority to true changes this behavior.
        // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        root.setParentLoaderPriority(true);

        final String webappDirLocation = "src/main/webapp/";
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);

        server.setHandler(root);

        server.start();
        server.join();
    }
}
