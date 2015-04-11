package net.capps.word.rest.providers;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.capps.word.db.dao.UsersDAO;
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

/**
 * Created by charlescapps on 4/9/15.
 */
public class OneSignalProvider {
    private static final OneSignalProvider INSTANCE = new OneSignalProvider();
    private static final UsersDAO usersDAO = UsersDAO.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(OneSignalProvider.class);

    private static final String ONE_SIGNAL_NOTIFICATIONS_URI = "https://onesignal.com/api/v1/notifications";
    private static final String ONE_SIGNAL_APP_ID = "479f3518-dbfa-11e4-ac8e-a310507ee73c";
    private static final String ONE_SIGNAL_REST_KEY = "NDc5ZjM1OWEtZGJmYS0xMWU0LWFjOGYtNjdjZjBiMzEwYTk1";
    private static final String AUTHZ_HEADER = "Basic " + ONE_SIGNAL_REST_KEY;

    private Client CLIENT = null;

    private OneSignalProvider() { }

    public static OneSignalProvider getInstance() {
        return INSTANCE;
    }

    public void sendPushNotificationForMove(GameModel originalGame, GameModel updatedGame) throws SQLException {
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

        if (CLIENT == null) {
            CLIENT = createOneSignalClient();
        }

        final OneSignalTagModel tag = new OneSignalTagModel("ghostwriters_id", Integer.toString(currentUser.getId()), "=");
        final OneSignalContentModel contents = new OneSignalContentModel("It's your move in your game with " + opponentUser.getUsername() + ".");
        final OneSignalContentModel headings = new OneSignalContentModel("It's your move!");

        OneSignalNotificationModel notification = new OneSignalNotificationModel(
                ONE_SIGNAL_APP_ID,
                contents,
                headings,
                Lists.newArrayList(tag));

      //  notification.setIsAndroid(true);
      //  notification.setIsIos(true);

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

    // -------- Private ----------
    private static Client createOneSignalClient() {
        SslConfigurator sslConfigurator = SslConfigurator.newInstance()
                .securityProtocol("SSL");
        SSLContext sslContext = sslConfigurator.createSSLContext();
        return ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .build();
    }
}
