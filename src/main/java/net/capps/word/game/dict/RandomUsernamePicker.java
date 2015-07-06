package net.capps.word.game.dict;

import net.capps.word.db.dao.UsersDAO;
import net.capps.word.rest.models.UserModel;
import net.capps.word.util.RandomUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static net.capps.word.rest.providers.UsersProvider.MAX_USERNAME_LEN;

/**
 * Created by charlescapps on 3/14/15.
 */
public class RandomUsernamePicker {
    private static final RandomUsernamePicker INSTANCE = new RandomUsernamePicker();
    private static final DictionarySet adjectiveSet = Dictionaries.getAdjectivesSet();
    private static final DictionaryPicker adjectivePicker = Dictionaries.getAdjectivesPicker();
    private static final DictionaryPicker nounPicker = Dictionaries.getNounsPicker();
    private static final int MAX_ADJECTIVE_TRIES = 10;
    private static final int MAX_NOUN_TRIES = 10;

    public static RandomUsernamePicker getInstance() {
        return INSTANCE;
    }

    public Optional<String> generateRandomUsername() throws SQLException {
        List<String> randomAdjectives = RandomUtil.shuffleList(adjectiveSet.getWordList());

        String username;
        Optional<UserModel> conflictUser;
        for (int i = 0; i < MAX_ADJECTIVE_TRIES; i++ ) {
            // Get adjectives from a precomputed random list to eliminate duplicate choices
            String adjective = randomAdjectives.get(i);
            for (int j = 0; j < MAX_NOUN_TRIES; j++) {
                int remainingLen = MAX_USERNAME_LEN - adjective.length();
                String noun = nounPicker.getRandomWordBetweenLengths(2, remainingLen);
                username = uppercase(adjective) + uppercase(noun);
                conflictUser = UsersDAO.getInstance().getUserByUsername(username, false);
                if (!conflictUser.isPresent()) {
                    return Optional.of(username);
                }
            }
        }

        // Last resort - add random numbers
        for (int i = 0; i < MAX_ADJECTIVE_TRIES; i++) {
            final String randomNum = Integer.toString(ThreadLocalRandom.current().nextInt(1000));
            final String adjective = adjectivePicker.getRandomWordBetweenLengths(2, MAX_USERNAME_LEN - randomNum.length() - 2);
            final int remainingLen = MAX_USERNAME_LEN - adjective.length() - randomNum.length();
            final String noun = nounPicker.getRandomWordBetweenLengths(2, remainingLen);

            username = uppercase(adjective) + uppercase(noun) + randomNum;
            conflictUser = UsersDAO.getInstance().getUserByUsername(username, false);
            if (!conflictUser.isPresent()) {
                return Optional.of(username);
            }
        }

        return Optional.empty();
    }

    private static String uppercase(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
