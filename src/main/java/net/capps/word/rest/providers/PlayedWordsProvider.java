package net.capps.word.rest.providers;

import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.PlayedWordsDAO;
import net.capps.word.game.dict.DictType;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.game.move.MoveType;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.WordModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by charlescapps on 6/2/15.
 */
public class PlayedWordsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PlayedWordsProvider.class);
    private static final PlayedWordsProvider INSTANCE = new PlayedWordsProvider();
    private static final PlayedWordsDAO playedWordsDAO = PlayedWordsDAO.getInstance();

    public static PlayedWordsProvider getInstance() {
        return INSTANCE;
    }

    public List<WordModel> getPlayedWordsForSpecialDict(int authUserId, SpecialDict specialDict) throws SQLException {
        Optional<String> wordMapOpt;
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            wordMapOpt = playedWordsDAO.getWordMap(dbConn, authUserId, specialDict);
        }

        if (!wordMapOpt.isPresent()) {
            return getSortedWordsForWordMap(specialDict, "");
        }

        String hexWordMap = wordMapOpt.get();
        return getSortedWordsForWordMap(specialDict, hexWordMap);
    }

    public void registerPlayedWordForMove(MoveModel moveModel, Connection dbConn) throws SQLException {
        if (moveModel.getMoveType() != MoveType.PLAY_WORD) {
            return;
        }
        DictType dictType = moveModel.getDict();
        if (dictType == null) {
            return;
        }

        SpecialDict specialDict = SpecialDict.ofDictType(dictType);
        if (specialDict == null) {
            return;
        }
        int userId = moveModel.getPlayerId();
        String word = moveModel.getLetters();
        registerPlayedWord(userId, word, specialDict, dbConn);
    }

    /**
     * @param playedWord - Uppercase word that must be in the specialDict
     */
    public void registerPlayedWord(int userId, String playedWord, SpecialDict specialDict, Connection dbConn) throws SQLException {
        LOG.info("Registering played word '{}' for dict '{}'", playedWord, specialDict);
        if (!specialDict.getDictType().getDictionary().contains(playedWord)) {
            LOG.error("ERROR - attempt to register a word '{}' that isn't in the given dict '{}'",
                    playedWord, specialDict);
            return;
        }

        Optional<String> existingHexMap = playedWordsDAO.getWordMap(dbConn, userId, specialDict);
        if (existingHexMap.isPresent()) {
            updateExistingWordMap(existingHexMap.get(), playedWord, userId, specialDict, dbConn);
            return;
        }
        insertNewWordMap(playedWord, userId, specialDict, dbConn);
    }

    // ---------- Private ------------

    private void insertNewWordMap(String playedWord, int userId, SpecialDict specialDict, Connection dbConn)
            throws SQLException {
        final DictionarySet dict = specialDict.getDictType().getDictionary();
        Integer wordIndex = dict.getWordIndex(playedWord);
        if (wordIndex == null) {
            LOG.error("Bad attempt to insert word map - word '{}' wasn't in the special dict '{}'", playedWord, specialDict);
            return;
        }
        StringBuilder sb = new StringBuilder(wordIndex + 1);
        while (sb.length() < wordIndex) {
            sb.append('0');
        }
        sb.append('1');

        // Need a multiple of 8 digits to insert bytes into Postgres
        while (sb.length() % 8 != 0) {
            sb.append('0');
        }

        if (sb.charAt(wordIndex) != '1') {
            throw new RuntimeException("ERROR - invalid insertion in PlayedWordsProvider#insertNewWordMap. Expected the binary string to be '1' at the position of the word's index");
        }
        final String binaryString = sb.toString();
        final String hexString = binaryStringToHexString(binaryString);
        LOG.info("Inserting word '{}' to dict '{}' with index '{}' binary map {}", playedWord, specialDict, wordIndex, binaryString);
        playedWordsDAO.insertWordMap(dbConn, userId, specialDict, hexString);
    }

    private void updateExistingWordMap(String existingHexMap, String playedWord, int userId, SpecialDict specialDict, Connection dbConn)
            throws SQLException {
        final DictionarySet dict = specialDict.getDictType().getDictionary();
        Integer wordIndex = dict.getWordIndex(playedWord);
        if (wordIndex == null) {
            LOG.error("Bad attempt to update word map - word '{}' wasn't in the special dict '{}'", playedWord, specialDict);
            return;
        }

        final String binaryMap = hexWordMapToBinaryString(existingHexMap);

        StringBuilder sb = new StringBuilder(binaryMap);
        while (sb.length() < wordIndex + 1) {
            sb.append('0');
        }
        sb.setCharAt(wordIndex, '1');

        // Need a multiple of 8 digits to insert bytes into Postgres
        while (sb.length() % 8 != 0) {
            sb.append('0');
        }
        String updatedBinaryMap = sb.toString();
        String updatedHexMap = binaryStringToHexString(updatedBinaryMap);
        LOG.info("Updating wordmap with word '{}' for dict '{}' with index '{}' binary map of len {} = {}", playedWord, specialDict, wordIndex, updatedBinaryMap.length(), updatedBinaryMap);

        playedWordsDAO.updateWordMap(dbConn, userId, specialDict, updatedHexMap);
    }

    private List<WordModel> getSortedWordsForWordMap(SpecialDict specialDict, String hexWordMap) {
        final List<String> wordList = specialDict.getDictType().getDictionary().getWordList();
        final Map<String, String> defs = specialDict.getDictType().getDictionary().getDefinitions();
        final List<WordModel> wordModels = new ArrayList<>(wordList.size());

        LOG.info("Getting sorted words for hex word map: '{}'", hexWordMap);
        String binaryWordMap = hexWordMapToBinaryString(hexWordMap);

        final int maxPlayedWord = Math.min(wordList.size(), binaryWordMap.length());
        int i = 0;
        for (; i < maxPlayedWord; ++i) {
            String word = wordList.get(i);
            boolean played = '1' == binaryWordMap.charAt(i);
            WordModel wordModel = new WordModel(defs.get(word), word, played);
            wordModels.add(wordModel);
        }
        for (; i < wordList.size(); ++i) {
            // Any words without corresponding digits in the word map are implicitly not played
            String word = wordList.get(i);
            WordModel wordModel = new WordModel(defs.get(word), word, false);
            wordModels.add(wordModel);
        }

        Collections.sort(wordModels);
        return wordModels;
    }

    private String hexWordMapToBinaryString(String hexMap) {
        if (hexMap.isEmpty()) {
            return "";  // An empty string is equivalent to a string of all 0's. No words have been played.
        }
        String binaryString = new BigInteger(hexMap, 16).toString(2);
        StringBuilder sb = new StringBuilder(binaryString);
        final int binaryLen = hexMap.length() * 4;
        while (sb.length() < binaryLen) {
            sb.append('0');
        }
        return sb.toString();
    }

    private String binaryStringToHexString(String binaryString) {
        if (binaryString.isEmpty()) {
            return "";  // An empty string is equivalent to a string of all 0's. No words have been played.
        }
        StringBuilder sb = new StringBuilder(binaryString.length() / 4);
        for (int i = 0; i < binaryString.length() / 4; ++i) {
            int start = i * 4;
            String substring = binaryString.substring(start, start + 4);
            int n = Integer.parseInt(substring, 2);
            String hex = Integer.toHexString(n);
            sb.append(hex);
        }
        return sb.toString();
    }
}
