package application;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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

    private ArrayList<WebSocketAdapter> listeners = new ArrayList<>();

    public CoinbaseClient() {
    }

    void addListener(WebSocketAdapter listener) {
        this.listeners.add(listener);
    }

    void removeListener(WebSocketAdapter listener) {
        for (WebSocketAdapter socketListener : listeners) {
            if (socketListener == listener) {
                listeners.remove(socketListener);
                break;
            }
        }
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

            SocketListener mainListener = new SocketListener();

            // Trys to connect.
            // Future.class here lets us treat the connect() as an asynchronous
            // receive (IRecv).
            Future<Session> fSession = client.connect(mainListener, uri);

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

    private class SocketListener extends WebSocketAdapter {
        @Override
        public void onWebSocketConnect(Session sess) {
            super.onWebSocketConnect(sess);
            for (WebSocketAdapter listener : listeners) {
                listener.onWebSocketConnect(sess);
            }
        }

        @Override
        public void onWebSocketText(String message) {
            super.onWebSocketText(message);
            for (WebSocketAdapter listener : listeners) {
                listener.onWebSocketText(message);
            }
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            super.onWebSocketClose(statusCode, reason);
            for (WebSocketAdapter listener : listeners) {
                listener.onWebSocketClose(statusCode, reason);
            }
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            super.onWebSocketError(cause);
            for (WebSocketAdapter listener : listeners) {
                listener.onWebSocketError(cause);
            }
        }
    }
}