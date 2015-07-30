package net.capps.word.db.dao;

import net.capps.word.db.WordDbManager;
import net.capps.word.exceptions.WordDbException;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UserRecordChange;
import net.capps.word.util.RandomUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.sql.Connection;

/**
 * Created by charlescapps on 7/29/15.
 */
public class UsersDAOTest {
    private static final int NUM_USERS = 10000;

    private static final int MIN_RATING = 1000;
    private static final int MAX_RATING = 10000;

    @Test
    public void testCreateManyUsersAndQueryLeaderboard() throws Exception {
        final UsersDAO usersDAO = UsersDAO.getInstance();

        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            for (int i = 0; i < NUM_USERS; ++i) {
                createUserWithRandomRating(i, usersDAO, dbConn);
            }
        }
    }

    private UserModel createUserWithRandomRating(int index, UsersDAO usersDAO, Connection dbConn) throws Exception {
        final String username = "User_" + index + "_" + RandomStringUtils.randomAlphanumeric(4);
        UserModel inputUser = new UserModel(null, username, null, null, null, false);
        UserModel createdUser = usersDAO.insertNewUser(dbConn, inputUser);

        final int rating = RandomUtil.randomInt(MIN_RATING, MAX_RATING);
        usersDAO.updateUserRating(dbConn, createdUser.getId(), rating, UserRecordChange.INCREASE_WINS);

        return createdUser;
    }
}
