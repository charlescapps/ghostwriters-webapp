package net.capps.word.rest.providers;

import com.google.common.collect.Lists;
import net.capps.word.game.common.GameResult;
import net.capps.word.game.common.GameType;
import net.capps.word.rest.models.*;
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
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by charlescapps on 4/9/15.
 */
public class OneSignalProvider {
    private static final OneSignalProvider INSTANCE = new OneSignalProvider();
    private static final Logger LOG = LoggerFactory.getLogger(OneSignalProvider.class);

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
            Future<Void> future = pool.submit(
                    () -> {
                        sendPushNotificationForMove(originalGame, updatedGame);
                        return null;
                    }
            );

            future.get();
        } catch (Exception e) {
            LOG.error("Error sending push notification:", e);
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
        UserModel opponentUser = updatedGame.getPlayer1Turn() ? updatedGame.getPlayer2Model() : updatedGame.getPlayer1Model();

        if (Boolean.TRUE.equals(currentUser.getSystemUser())) {
            // Don't send notifications to the AI players...but this should never happen.
            return;
        }

        // Check for the first move being played on a newly created two-player game
        if (originalGame.getGameResult() == GameResult.OFFERED && updatedGame.getGameResult() == GameResult.OFFERED) {
            final String title = "A Challenger Awaits";
            final String message = "Challenge by " + opponentUser.getUsername();
            sendPushNotification(currentUser, updatedGame, title, message, true);
        } else {
            final String title = "It's your move!";
            final String message = "It's your move vs. " + opponentUser.getUsername();
            sendPushNotification(currentUser, updatedGame, title, message, false);
        }
    }

    // -------- Private ----------
    private void sendPushNotification(UserModel currentUser, GameModel updatedGame, String title, String message, boolean isGameOffer) {
        if (CLIENT == null) {
            CLIENT = createOneSignalClient();
        }

        final PushTagModel tag = new PushTagModel("ghostwriters_id", Integer.toString(currentUser.getId()), "=");
        final PushContentModel contents = new PushContentModel(message);
        final PushContentModel headings = new PushContentModel(title);

        OneSignalNotificationModel notification = new OneSignalNotificationModel(
                ONE_SIGNAL_APP_ID,
                contents,
                headings,
                Lists.newArrayList(tag));

        PushData pushData = new PushData(Integer.toString(updatedGame.getId()));
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
            LOG.info("SUCCESS - Received 200 response from sending push notification to player {} ({})", currentUser.getUsername(), currentUser.getId());
        }
        LOG.info("Response body: {}", responseBody);
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
