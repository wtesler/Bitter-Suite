package application;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

public class CoinbaseClient {

    public static final String COINBASE_MARKET_URL = "wss://ws-feed.exchange.coinbase.com";

    public static final String KEY_TYPE = "type";
    public static final String KEY_PID = "product_id";

    public static final String MESSAGE_SUBSCRIBE = "subscribe";
    public static final String MESSAGE_BITCOIN_USD = "BTC-USD";

    // client creates a socketed session between us and the coinbase server.
    WebSocketClient client;
    Session session;

    // Defined inside this class are a set of callbacks that the main thread overrides.
    // Usually it is the case that this class is extended.
    WebSocketAdapter listener;

    public CoinbaseClient(WebSocketAdapter listener) {
        this.listener = listener;
    }

    public void start() {

        try {

            // This the access point to the Coinbase Market Data API.
            URI uri = URI.create(COINBASE_MARKET_URL);

            // Retrieving Market Data is NOT a strictly secure connection...
            // Currently: Trusting All.
            SslContextFactory sslContextFactory = new SslContextFactory(true);

            // Establishes a session between Coinbase and us.
            client = new WebSocketClient(sslContextFactory);

            // Prepare the Client
            try {
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Trys to connect.
            // Future.class here lets us treat the connect() as an asynchronous
            // receive (IRecv).
            Future<Session> fSession = client.connect(listener, uri);

            // Maybe do something here...

            // Wait for Connect
            session = fSession.get();

            // Construct the subscribe message as defined in the API.
            JSONObject json = new JSONObject();
            json.put(KEY_TYPE, MESSAGE_SUBSCRIBE);
            json.put(KEY_PID, MESSAGE_BITCOIN_USD);

            // Send the subscribe message to the server.
            session.getRemote().sendString(json.toString());

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace(System.err);
        }
    }

    public void stop() {
        try {
            session.close();
            client.stop();
        } catch (Exception e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
    }
}