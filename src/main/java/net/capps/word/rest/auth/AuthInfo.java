package net.capps.word.rest.auth;

import com.google.common.base.Preconditions;
import net.capps.word.models.UserModel;

/**
 * Created by charlescapps on 12/28/14.
 */
public class AuthInfo {
    private final UserModel authUser;
    private final String sessionId;

    public AuthInfo(UserModel authUser, String sessionId) {
        this.authUser = Preconditions.checkNotNull(authUser);
        this.sessionId = Preconditions.checkNotNull(sessionId);
    }

    public UserModel getAuthUser() {
        return authUser;
    }

    public String getSessionId() {
        return sessionId;
    }
}
