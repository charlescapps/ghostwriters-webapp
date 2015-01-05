package net.capps.word.db.dao;

/**
 * Created by charlescapps on 1/4/15.
 */
public class UserHashInfo {
    private final String hashPass;
    private final String salt;

    public UserHashInfo(String hashPass, String salt) {
        this.hashPass = hashPass;
        this.salt = salt;
    }

    public String getHashPass() {
        return hashPass;
    }

    public String getSalt() {
        return salt;
    }
}
