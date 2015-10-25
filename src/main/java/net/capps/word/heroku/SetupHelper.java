package net.capps.word.heroku;

import net.capps.word.constants.WordConstants;
import net.capps.word.db.TableDefinitions;
import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.db.dao.VersionDAO;
import net.capps.word.game.board.FixedLayouts;
import net.capps.word.game.dict.Dictionaries;
import net.capps.word.game.gen.PositionLists;
import net.capps.word.game.tile.LetterPoints;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;
import net.capps.word.versioning.UpgradeHelper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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

        try(Connection dbConn = wordDbManager.getConnection()) {

            createWordUsersTable(dbConn);

            createWordGamesTable(dbConn);

            createWordMovesTable(dbConn);

            createWordSessionsTable(dbConn);

            createPlayedWordsTable(dbConn);

            createWordVersionInfoTable(dbConn);

            // Perform upgrades...
            UpgradeHelper.getInstance().performNeededUpgrades();
        }
    }

    private void createWordUsersTable(Connection dbConn) throws SQLException {
        // --------- word_users table ----------
        Statement stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_WORD_USERS_TABLE);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_DEVICE_ID_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_DEVICE_ID_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_USERNAME_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_USERNAME_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_LOWERCASE_USERNAME_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_LOWERCASE_USERNAME_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_USER_RATING_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_USER_RATING_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_RATING_DESC_AND_ID_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_RATING_DESC_AND_ID_IDX);
    }

    private void createWordGamesTable(Connection dbConn) throws SQLException {
        // -------- word_games table --------
        Statement stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_GAMES_TABLE);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_PLAYER1_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_PLAYER1_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_PLAYER2_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_PLAYER2_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_LAST_ACTIVITY_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_LAST_ACTIVITY_IDX);
    }

    private void createWordMovesTable(Connection dbConn) throws SQLException {
        // -------- word_moves table -------
        Statement stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_MOVES_TABLE);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_GAME_ID_FOR_MOVES_TABLE_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_GAME_ID_FOR_MOVES_TABLE_IDX);
    }

    private void createWordSessionsTable(Connection dbConn) throws SQLException {
        // -------- word_sessions table
        Statement stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_SESSION_TABLE);
    }

    private void createPlayedWordsTable(Connection dbConn) throws SQLException {
        // --------- played_words table ------
        Statement stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_PLAYED_WORDS_TABLE);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_PLAYED_WORDS_USER_ID_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_PLAYED_WORDS_USER_ID_IDX);
    }

    private void createWordVersionInfoTable(Connection dbConn) throws SQLException {
        // --------- played_words table ------
        Statement stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_VERSION_INFO_TABLE);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.DROP_VERSION_IDX);

        stmt = dbConn.createStatement();
        stmt.executeUpdate(TableDefinitions.CREATE_VERSION_INDEX);

        VersionDAO.getInstance().insertCurrentVersionIfNotPresent(dbConn);
    }

    public void createAiUsers() throws Exception {
        UserModel randomUser = new UserModel(null, WordConstants.RANDOM_AI_USERNAME, null, null, null, true);
        UserModel bookwormUser = new UserModel(null, WordConstants.BOOKWORM_AI_USERNAME, null, null, null, true);
        UserModel professorUser = new UserModel(null, WordConstants.PROFESSOR_AI_USERNAME, null, null, null, true);

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            usersProvider.createNewUserIfNotExists(dbConn, randomUser);
            usersProvider.createNewUserIfNotExists(dbConn, bookwormUser);
            usersProvider.createNewUserIfNotExists(dbConn, professorUser);

            Optional<UserModel> randomUserOpt = usersDAO.getUserByUsername(dbConn, WordConstants.RANDOM_AI_USERNAME, true);
            Optional<UserModel> bookwormUserOpt = usersDAO.getUserByUsername(dbConn, WordConstants.BOOKWORM_AI_USERNAME, true);
            Optional<UserModel> profUserOpt = usersDAO.getUserByUsername(dbConn, WordConstants.PROFESSOR_AI_USERNAME, true);

            // They must be present since we just inserted them into the Database!
            WordConstants.RANDOM_AI_USER.set(randomUserOpt.get());
            WordConstants.BOOKWORM_AI_USER.set(bookwormUserOpt.get());
            WordConstants.PROFESSOR_AI_USER.set(profUserOpt.get());
        }
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
