package net.capps.word.rest.providers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.capps.word.db.WordDbManager;
import net.capps.word.db.dao.UsersDAO;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.common.GameType;
import net.capps.word.rest.models.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.SslConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by charlescapps on 4/9/15.
 */
public class OneSignalProvider {
    private static final OneSignalProvider INSTANCE = new OneSignalProvider();
    private static final Logger LOG = LoggerFactory.getLogger(OneSignalProvider.class);

    private static final UsersDAO usersDAO = UsersDAO.getInstance();

    private static final String ONE_SIGNAL_NOTIFICATIONS_URI = "https://onesignal.com/api/v1/notifications";
    private static final String ONE_SIGNAL_APP_ID = "479f3518-dbfa-11e4-ac8e-a310507ee73c";
    private static final String ONE_SIGNAL_REST_KEY = "NDc5ZjM1OWEtZGJmYS0xMWU0LWFjOGYtNjdjZjBiMzEwYTk1";
    private static final String AUTHZ_HEADER = "Basic " + ONE_SIGNAL_REST_KEY;

    private static final int NUM_THREADS = 10;
    private ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

    private Client CLIENT = null;

    private OneSignalProvider() { }

    public static OneSignalProvider getInstance() {
        return INSTANCE;
    }

    public void sendPushNotificationForMoveAsync(GameModel originalGame, GameModel updatedGame) {
        try {
            pool.submit(
                    () -> {
                        try {
                            sendPushNotificationForMove(originalGame, updatedGame);
                        } catch (Exception e) {
                            LOG.error("Error sending push notification:", e);
                        }
                        return null;
                    }
            );

        } catch (Exception e) {
            LOG.error("Error submitting task to send push notification:", e);
        }
    }

    public void sendPushNotificationForMove(GameModel originalGame, GameModel updatedGame) throws SQLException {
        if (originalGame.getGameType() != GameType.TWO_PLAYER) {
            // Do nothing if it's a single-player game.
            return;
        }
        if (originalGame.getPlayer1Turn() == updatedGame.getPlayer1Turn()) {
            // Do nothing if the turn didn't change
            return;
        }

        UserModel currentUser = updatedGame.getPlayer1Turn() ? updatedGame.getPlayer1Model() : updatedGame.getPlayer2Model();

        if (Boolean.TRUE.equals(currentUser.getSystemUser())) {
            // Don't send notifications to the AI players...but this should never happen.
            return;
        }

        Optional<String> oneSignalIdOpt;
        try (Connection dbConn = WordDbManager.getInstance().getConnection()) {
            oneSignalIdOpt = usersDAO.getOneSignalIdOpt(dbConn, currentUser.getId());
        }

        if (!oneSignalIdOpt.isPresent()) {
            // Don't send notifications if we never received a OneSignal player id from the client
            return;
        }
        final String oneSignalId = oneSignalIdOpt.get();

        // Check for the first move being played on a newly created two-player game
        final GameResult ORIGINAL_RESULT = originalGame.getGameResult();
        final GameResult UPDATED_RESULT = updatedGame.getGameResult();
        Pair<String, String> titleAndMessage = getPushTitleAndMessage(originalGame, updatedGame);
        sendPushNotification(currentUser,
                             updatedGame,
                             titleAndMessage.getLeft(),
                             titleAndMessage.getRight(),
                             ORIGINAL_RESULT == GameResult.OFFERED && UPDATED_RESULT == GameResult.OFFERED,
                             oneSignalId);
    }

    public Optional<ErrorModel> validateOneSignalInfo(UserModel authUser, OneSignalInfoModel oneSignalInfoModel) {
        if (oneSignalInfoModel == null) {
            return Optional.of(new ErrorModel("Must provide One Signal info"));
        }
        if (oneSignalInfoModel.getUserId() == null) {
            return Optional.of(new ErrorModel("Must provide userId field."));
        }
        if (Strings.isNullOrEmpty(oneSignalInfoModel.getOneSignalPlayerId())) {
            return Optional.of(new ErrorModel("Must provide oneSignalPlayerId field."));
        }
        if (!oneSignalInfoModel.getUserId().equals(authUser.getId())) {
            return Optional.of(new ErrorModel("The userId must match the currently authenticated user!"));
        }

        return Optional.empty();
    }


    // ------------- PRIVATE ------------

    private Pair<String, String> getPushTitleAndMessage(GameModel originalGame, GameModel updatedGame) {
        UserModel currentUser = updatedGame.getPlayer1Turn() ? updatedGame.getPlayer1Model() : updatedGame.getPlayer2Model();
        UserModel opponentUser = updatedGame.getPlayer1Turn() ? updatedGame.getPlayer2Model() : updatedGame.getPlayer1Model();
        final GameResult ORIGINAL_RESULT = originalGame.getGameResult();
        final GameResult UPDATED_RESULT = updatedGame.getGameResult();

        if (ORIGINAL_RESULT == GameResult.OFFERED && UPDATED_RESULT == GameResult.OFFERED) {
            return ImmutablePair.of("A Challenger Awaits",
                                    "Challenge by " + opponentUser.getUsername());
        } else if (UPDATED_RESULT == GameResult.PLAYER1_WIN) {
            if (currentUser.getId().equals(updatedGame.getPlayer1())) {
                return ImmutablePair.of("You won!",
                                        "You beat " + opponentUser.getUsername() + "!");
            } else {
                return ImmutablePair.of("Game Over",
                                        "Game Over vs. " + opponentUser.getUsername());
            }
        } else if (UPDATED_RESULT == GameResult.PLAYER2_WIN) {
            if (currentUser.getId().equals(updatedGame.getPlayer2())) {
                return ImmutablePair.of("You won!",
                                        "You beat " + opponentUser.getUsername() + "!");
            } else {
                return ImmutablePair.of("Game Over",
                                        "Game Over vs. " + opponentUser.getUsername());
            }
        } else if (UPDATED_RESULT == GameResult.TIE) {
            return ImmutablePair.of("It's a tie!", "You tied " + opponentUser.getUsername());
        } else {
            return ImmutablePair.of("It's your move!",
                                    "It's your move vs. " + opponentUser.getUsername());
        }
    }

    // -------- Private ----------
    private void sendPushNotification(UserModel currentUser, GameModel updatedGame, String title, String message, boolean isGameOffer, String oneSignalId) {
        if (CLIENT == null) {
            CLIENT = createOneSignalClient();
        }

        final List<String> include_player_ids = Lists.newArrayList(oneSignalId);
        final PushContentModel contents = new PushContentModel(message);
        final PushContentModel headings = new PushContentModel(title);

        OneSignalNotificationModel notification = new OneSignalNotificationModel(
                ONE_SIGNAL_APP_ID,
                contents,
                headings,
                null,
                include_player_ids);

        PushData pushData = new PushData(Integer.toString(updatedGame.getId()));

        // Set the target user ID so the app can verify the push notification isn't for some other user that
        // was previously logged in on the same device.
        pushData.setTargetUserId(currentUser.getId().toString());
        pushData.setTargetUsername(currentUser.getUsername());

        if (isGameOffer) {
            pushData.setIsGameOffer(Boolean.TRUE.toString()); // Only strings are allowed for this additional data...
            pushData.setBoardSize(toStringOrNull(updatedGame.getBoardSize()));
            pushData.setSpecialDict(toStringOrNull(updatedGame.getSpecialDict()));
            pushData.setGameDensity(toStringOrNull(updatedGame.getGameDensity()));
            pushData.setBonusesType(toStringOrNull(updatedGame.getBonusesType()));
        }

        notification.setData(pushData);
        notification.setIsAndroid(true);
        notification.setIsIos(true);

        Response response = CLIENT.target(ONE_SIGNAL_NOTIFICATIONS_URI)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", AUTHZ_HEADER)
                .post(Entity.entity(notification, MediaType.APPLICATION_JSON_TYPE));

        String responseBody = null;
        if (response.hasEntity()) {
            responseBody = response.readEntity(String.class);
        }

        if (response.getStatusInfo().getStatusCode() != HttpStatus.OK_200) {
            LOG.error("FAIL - Received non-200 response from OneSignal: {}", response.getStatusInfo().getStatusCode());
        } else {
            LOG.debug("SUCCESS - Received 200 response from sending push notification to player {} ({})", currentUser.getUsername(), currentUser.getId());
        }
        LOG.debug("Response body: {}", responseBody);
    }

    private static String toStringOrNull(Object o) {
        return o == null ? null : o.toString();
    }

    private static Client createOneSignalClient() {
        SslConfigurator sslConfigurator = SslConfigurator.newInstance()
                .securityProtocol("SSL");
        SSLContext sslContext = sslConfigurator.createSSLContext();
        return ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .build();
    }
}
