package net.capps.word.heroku;

import com.google.common.base.Optional;
import net.capps.word.constants.WordConstants;
import net.capps.word.db.TableDefinitions;
import net.capps.word.db.WordDbManager;
import net.capps.word.models.ErrorModel;
import net.capps.word.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by charlescapps on 12/28/14.
 */
public class SetupHelper {

    private static final SetupHelper INSTANCE = new SetupHelper();

    private SetupHelper() {}

    public static SetupHelper getInstance() {
        return INSTANCE;
    }

    public void initDatabase() throws Exception {
        WordDbManager wordDbManager = WordDbManager.getInstance();

        try(Connection connection = wordDbManager.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DROP TABLE IF EXISTS word_users"); // For debugging, drop the table every time the server starts

            stmt = connection.createStatement();
            stmt.executeUpdate(TableDefinitions.CREATE_WORD_USERS_TABLE);
        }
    }

    public void createInitialUser() throws Exception {
        UserModel initialUser = new UserModel(null, WordConstants.INITIAL_USER_USERNAME, null, WordConstants.INITIAL_USER_PASSWORD, null);
        Optional<ErrorModel> initialUserError = UsersProvider.getInstance().validateInputUser(initialUser);
        if (initialUserError.isPresent()) {
            throw new Exception("Invalid initial user in setup: " + initialUserError.get());
        }
        UsersProvider.getInstance().createNewUser(initialUser);
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
