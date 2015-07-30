package net.capps.word.rest.providers;

import net.capps.word.db.dao.SessionsDAO;
import net.capps.word.rest.models.SessionModel;
import net.capps.word.rest.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by charlescapps on 2/5/15.
 */
public class SessionProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SessionProvider.class);
    private static final SessionProvider INSTANCE = new SessionProvider();
    private static final SessionsDAO sessionsDao = SessionsDAO.getInstance();

    public static SessionProvider getInstance() {
        return INSTANCE;
    }

    private SessionProvider() { } // Singleton pattern

    public SessionModel createNewSession(Connection dbConn, UserModel user) throws SQLException {
        sessionsDao.deleteSessionForUser(user.getId());

        String sessionId;
        do {
            sessionId = UUID.randomUUID().toString();
        } while (sessionsDao.getSessionForSessionId(dbConn, sessionId).isPresent());

        SessionModel session = sessionsDao.insertSession(dbConn, user.getId(), sessionId);
        LOG.debug("SUCCESS - created new session for user '{}': {}", user.getUsername(), session);
        return session;
    }

}
