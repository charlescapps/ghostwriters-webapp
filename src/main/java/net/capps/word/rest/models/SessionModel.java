package net.capps.word.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import net.capps.word.rest.auth.AuthHelper;

import javax.ws.rs.core.NewCookie;

/**
 * Created by charlescapps on 2/5/15.
 */
public class SessionModel {
    private final long id;
    private final String sessionId;
    private final int userId;
    private final long dateCreated;

    public SessionModel(long id, String sessionId, int userId, long dateCreated) {
        this.id = id;
        this.sessionId = Preconditions.checkNotNull(sessionId);
        this.userId = userId;
        this.dateCreated = dateCreated;
    }

    public long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    @JsonIgnore
    public NewCookie getNewCookie() {
        return new NewCookie(AuthHelper.COOKIE_NAME, sessionId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("sessionId", sessionId)
                .add("usedId", userId)
                .toString();
    }
}
