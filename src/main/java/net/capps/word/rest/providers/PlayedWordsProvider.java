package net.capps.word.rest.providers;

import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.PlayedWordsDAO;
import net.capps.word.game.dict.DictType;
import net.capps.word.game.dict.DictionarySet;
import net.capps.word.game.dict.SpecialDict;
import net.capps.word.game.move.MoveType;
import net.capps.word.rest.models.MoveModel;
import net.capps.word.rest.models.WordModel;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
    private static final Hex HEX = new Hex();
    private static final BinaryCodec BINARY_CODEC = new BinaryCodec();

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
        registerPlayedWords(userId, moveModel.getSpecialWordsPlayed(), specialDict, dbConn);
    }

    /**
     * @param playedWords - list of words from specialDict that were played this move.
     */
    public void registerPlayedWords(int userId, List<String> playedWords, SpecialDict specialDict, Connection dbConn) throws SQLException {
        if (playedWords.isEmpty()) {
            return;
        }
        for (String playedWord: playedWords) {
            if (!specialDict.getDictType().getDictionary().contains(playedWord)) {
                LOG.error("ERROR - attempt to register a word '{}' that isn't in the given dict '{}'",
                        playedWord, specialDict);
                return;
            }
        }

        Optional<String> existingHexMapOpt = playedWordsDAO.getWordMap(dbConn, userId, specialDict);
        if (existingHexMapOpt.isPresent()) {
            String hexMap = existingHexMapOpt.get();
            for (String playedWord: playedWords) {
                hexMap = updateExistingWordMap(hexMap, playedWord, specialDict);
            }
            playedWordsDAO.updateWordMap(dbConn, userId, specialDict, hexMap);
            return;
        }

        String hexMap = createNewWordMap(playedWords.get(0), specialDict);

        for (int i = 1; i < playedWords.size(); ++i) {
            String playedWord = playedWords.get(i);
            hexMap = updateExistingWordMap(hexMap, playedWord, specialDict);
        }
        playedWordsDAO.insertWordMap(dbConn, userId, specialDict, hexMap);
    }

    // ---------- Private ------------
    @Nonnull
    private String createNewWordMap(String playedWord, SpecialDict specialDict)
            throws SQLException {
        final DictionarySet dict = specialDict.getDictType().getDictionary();
        Integer wordIndex = dict.getWordIndex(playedWord);
        if (wordIndex == null) {
            LOG.error("Bad attempt to insert word map - word '{}' wasn't in the special dict '{}'", playedWord, specialDict);
            return "";
        }
        int expectedLen = wordIndex + 1;
        if (expectedLen % 8 != 0) {
            expectedLen += (8 - (expectedLen % 8));
        }
        LOG.info("WordIndex + 1 = {}, expectedLen = {}", wordIndex + 1, expectedLen);
        StringBuilder sb = new StringBuilder(expectedLen);
        while (sb.length() < wordIndex) {
            sb.append('0');
        }
        sb.append('1');

        // Need a multiple of 8 digits to insert bytes into Postgres
        while (sb.length() % 8 != 0) {
            sb.append('0');
        }

        final String binaryString = sb.toString();
        return binaryStringToHexString(binaryString);
    }

    private String updateExistingWordMap(String existingHexMap, String playedWord, SpecialDict specialDict)
            throws SQLException {
        final DictionarySet dict = specialDict.getDictType().getDictionary();
        Integer wordIndex = dict.getWordIndex(playedWord);
        if (wordIndex == null) {
            LOG.error("Bad attempt to update word map - word '{}' wasn't in the special dict '{}'", playedWord, specialDict);
            return existingHexMap;
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
        return binaryStringToHexString(updatedBinaryMap);

    }

    private List<WordModel> getSortedWordsForWordMap(SpecialDict specialDict, String hexWordMap) {
        final List<String> wordList = specialDict.getDictType().getDictionary().getWordList();
        final Map<String, String> defs = specialDict.getDictType().getDictionary().getDefinitions();
        final List<WordModel> wordModels = new ArrayList<>(wordList.size());

        LOG.trace("Getting sorted words for hex word map: '{}'", hexWordMap);
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
        try {
            byte[] rawBytes = HEX.decode(hexMap.getBytes());
            byte[] binaryChars = BINARY_CODEC.encode(rawBytes);
            return new String(binaryChars);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    private String binaryStringToHexString(String binaryString) {
        if (binaryString.length() % 8 != 0) {
            throw new IllegalArgumentException("Must provide a binary string whose len is a multiple of 8 to encode into hex!");
        }
        if (binaryString.isEmpty()) {
            return "";  // An empty string is equivalent to a string of all 0's. No words have been played.
        }
        byte[] rawBytes = BINARY_CODEC.decode(binaryString.getBytes());
        byte[] hexChars = HEX.encode(rawBytes);
        return new String(hexChars);
    }
}
