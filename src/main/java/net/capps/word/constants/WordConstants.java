package net.capps.word.constants;

import net.capps.word.rest.models.UserModel;
import net.capps.word.util.SingletonHolder;

/**
 * Created by charlescapps on 12/28/14.
 */
public class WordConstants {
    public static final String INITIAL_USER_USERNAME = "Initial User";
    public static final String INITIAL_USER_PASSWORD = "rL4JDxPyPRprsr6e";
    public static final SingletonHolder<UserModel> INITIAL_USER = SingletonHolder.absent();

    public static final String RANDOM_AI_USERNAME = "Monkey";
    public static final String BOOKWORM_AI_USERNAME = "Bookworm";
    public static final String PROFESSOR_AI_USERNAME = "Professor";

    public static final SingletonHolder<UserModel> RANDOM_AI_USER = SingletonHolder.absent();
    public static final SingletonHolder<UserModel> BOOKWORM_AI_USER = SingletonHolder.absent();
    public static final SingletonHolder<UserModel> PROFESSOR_AI_USER = SingletonHolder.absent();

    public static final String SCRABBLE_DICT_FILE = "net/capps/word/dict/scowl.words.95.lowercase";
}
