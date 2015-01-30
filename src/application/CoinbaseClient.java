package application;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Future;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

public class CoinbaseClient {

    // This client creates a socketed session between the coinbase server and
    // us.
    private WebSocketClient socketClient;
    // The socket lets us maintain a virtual session.
    private Session session;

    // A list of all the adapters that want to be updated when changes happen to
    // the coinbase client.
    private ArrayList<WebSocketAdapter> listeners = new ArrayList<>();

    public CoinbaseClient() {
        // Currently: Low Security
        SslContextFactory sslContextFactory1 = new SslContextFactory(true);
        // Create the Socket client.
        socketClient = new WebSocketClient(sslContextFactory1);
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

    public void openWebSocket(URI uri) throws Exception {

        // Prepare the Client
        socketClient.start();

        SocketListener mainListener = new SocketListener();

        // Trys to connect.
        // "Future" here means that we expect the client to return to us
        // a session sometime in the near future, but as of now, it is null.
        Future<Session> fSession = socketClient.connect(mainListener, uri);

        // Wait for the socket to return us a session.
        session = fSession.get();

        // Construct the subscribe message as defined in the API.
        JSONObject json = new JSONObject();
        json.put(Constants.TYPE, Constants.SUBSCRIBE);
        json.put(Constants.PRODUCT_ID, Constants.BITCOIN_USD);

        // Send the subscribe message to the server.
        session.getRemote().sendString(json.toString());

    }

    public void closeSocket() throws Exception {
        session.close();
        socketClient.stop();
    }

    /**
     * Get market match data from startDate to endDate and with a frequency
     * determined by granularity.
     *
     * @param startDate
     *            an ISO8601 time indicating the first data we are interested
     *            in.
     * @param endDate
     *            an ISO8601 time indicating the last data we are interested in.
     * @param granularity
     *            how often we want to sample the data (in seconds)
     */
    public static void getHistoricalData(Date startDate, Date endDate, int granularity)
            throws Exception {

        // Low Security needed
        SslContextFactory sslContextFactory2 = new SslContextFactory(true);
        // Create the HTTP client
        HttpClient httpClient = new HttpClient(sslContextFactory2);

        httpClient.start();

        // ISO 8601 format.
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        // Build the get request url.
        StringBuilder builder = new StringBuilder(Constants.COINBASE_API_ENDPOINT);
        builder.append("/products/" + Constants.BITCOIN_USD + "/candles").append('?');
        ;
        builder.append("start=" + df.format(startDate)).append('&');
        builder.append("end=" + df.format(endDate)).append('&');
        builder.append("granularity=" + granularity);

        // Issue a get request
        ContentResponse res = httpClient.GET(builder.toString());

        // Show the results
        System.out.println(res.getContentAsString());

        httpClient.stop();
    }

    public static void getOrderbook(ResponseDetail level) throws Exception {

        // Low Security needed
        SslContextFactory sslContextFactory2 = new SslContextFactory(true);
        // Create the HTTP client
        HttpClient httpClient = new HttpClient(sslContextFactory2);

        httpClient.start();

        // Build the get request using the Coinbase API.
        StringBuilder builder = new StringBuilder(Constants.COINBASE_API_ENDPOINT);
        builder.append("/products/" + Constants.BITCOIN_USD + "/book").append('?');
        builder.append("level=" + level.ordinal());

        // Issue a get request
        ContentResponse res = httpClient.GET(builder.toString());

        // Show the results
        System.out.println(res.getContentAsString());

        httpClient.stop();
    }

    /**
     * This class listens for changing on the socket. Those changes can include:
     * 1. Socket Opens 2. Text received 3. Socket closes 4. Socket error <br/>
     * <br/>
     * It then forwards the information it receives to all the listeners who
     * have been added to the CoinbaseClient.
     */
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