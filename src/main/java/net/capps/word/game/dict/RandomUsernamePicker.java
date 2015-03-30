package net.capps.word.game.dict;

import com.google.common.base.Optional;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.rest.models.UserModel;
import net.capps.word.rest.providers.UsersProvider;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by charlescapps on 3/14/15.
 */
public class RandomUsernamePicker {
    private static final RandomUsernamePicker INSTANCE = new RandomUsernamePicker();
    private static final DictionaryPicker adjectivePicker = Dictionaries.getAdjectivesPicker();
    private static final DictionaryPicker nounPicker = Dictionaries.getNounsPicker();
    private static final int MAX_TRIES = 5;

    public static RandomUsernamePicker getInstance() {
        return INSTANCE;
    }

    public Optional<String> generateRandomUsername() throws SQLException {
        String username;
        Optional<UserModel> conflictUser;
        for (int i = 0; i < MAX_TRIES; i++ ) {
            String adjective = adjectivePicker.getRandomWordEqualProbabilityByLength(3, 12);
            int remainingLen = UsersProvider.MAX_USERNAME_LEN - adjective.length();
            String noun = nounPicker.getRandomWordEqualProbabilityByLength(3, remainingLen);
            username = uppercase(adjective) + uppercase(noun);
            conflictUser = UsersDAO.getInstance().getUserByUsername(username, false);
            if (!conflictUser.isPresent()) {
                return Optional.of(username);
            }
        }

        // Last resort - add random numbers
        for (int i = 0; i < MAX_TRIES; i++) {
            String adjective = adjectivePicker.getRandomWordBetweenLengths(3, 12);
            int remainingLen = UsersProvider.MAX_USERNAME_LEN - adjective.length();
            String noun = nounPicker.getRandomWordBetweenLengths(3, remainingLen - 3);
            int randomNum = ThreadLocalRandom.current().nextInt(1000);
            username = uppercase(adjective) + uppercase(noun) + randomNum;
            conflictUser = UsersDAO.getInstance().getUserByUsername(username, false);
            if (!conflictUser.isPresent()) {
                return Optional.of(username);
            }
        }

        return Optional.absent();
    }

    private static String uppercase(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
