package application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.json.JSONObject;

public class LayoutController {
    @FXML
    Pane root;
    @FXML
    LineChart<Number, Number> lc_chart;

    NumberAxis xAxis;

    LineChart.Series<Number, Number> matchSeries;
    LineChart.Series<Number, Number> buySeries;
    LineChart.Series<Number, Number> sellSeries;

    CoinbaseClient client;

    // Stores the value of the last match price recorded.
    private double matchPrice = 0;

    // Stores the value of the last buy request recorded.
    private double buyPrice = 0;

    // Stores the value of the last sell request recorded.
    private double sellPrice = 0;

    private final static int MAX_DATA_SIZE = 500;

    // Coinbase API Metrics.
    private final static String MATCH_PRICE = "Match Price";
    private final static String RECEIVED_BUY = "Buy Request";
    private final static String RECEIVED_SELL = "Sell Request";

    // Coinbase API Message types.
    private final static String MATCH = "match";
    private final static String ERROR = "error";
    private final static String RECEIVED = "received";
    private final static String BUY = "buy";

    // Coinbase API Message keys.
    private final static String SEQUENCE = "sequence";
    private final static String MESSAGE = "message";
    private final static String PRICE = "price";
    private final static String TYPE = "type";
    private final static String SIDE = "side";

    public void initialize() {

        // Our X Axis
        xAxis = (NumberAxis) lc_chart.getXAxis();

        // The line representing buy requests.
        buySeries = new LineChart.Series<Number, Number>();
        buySeries.setName(RECEIVED_BUY);
        lc_chart.getData().add(buySeries);

        // The line representing sell requests.
        sellSeries = new LineChart.Series<Number, Number>();
        sellSeries.setName(RECEIVED_SELL);
        lc_chart.getData().add(sellSeries);

        // The line representing our match price.
        matchSeries = new LineChart.Series<Number, Number>();
        matchSeries.setName(MATCH_PRICE);
        lc_chart.getData().add(matchSeries);

        // Aesthetic preference.
        lc_chart.setCreateSymbols(false);

        // Create the CoinbaseClient. Receiving updates from the listener.
        client = new CoinbaseClient(listener);

        client.start();
    }

    public void stop() {
        client.stop();
    }

    WebSocketAdapter listener = new WebSocketAdapter() {

        @Override
        public void onWebSocketConnect(Session sess) {
            super.onWebSocketConnect(sess);
            System.out.println("Socket Connected: " + sess);
        }

        @Override
        public void onWebSocketText(String message) {
            super.onWebSocketText(message);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    // Parse the message that we've just received from Coinbase.
                    JSONObject json = new JSONObject(message);
                    String type = json.getString(TYPE);

                    // Base case: Coinbase sent us an error.
                    if (type.equals(ERROR)) {
                        System.err.println("Coinbase sent an error: " + json.getString(MESSAGE));
                        return;
                    }

                    // Parameters
                    long sequence = json.getLong(SEQUENCE);
                    double price = json.getDouble(PRICE);
                    String side = json.getString(SIDE);

                    // Update the chart depending on the type of message
                    // received.
                    if (type.equals(MATCH)) {
                        // Update the match price.
                        matchPrice = price;
                    } else if (type.equals(RECEIVED)) {
                        if (BUY.equals(side)) {
                            buyPrice = price;
                            if (buyPrice != 0) {
                                // Update the buy price.
                                buySeries.getData().add(
                                        new LineChart.Data<Number, Number>(sequence, buyPrice));
                            }
                        } else { // SELL
                            sellPrice = price;
                            if (sellPrice != 0) {
                                // Update the sell price.
                                sellSeries.getData().add(
                                        new LineChart.Data<Number, Number>(sequence, sellPrice));
                            }
                        }
                    }

                    // Always update the matchPrice (if not 0)
                    if (matchPrice != 0) {
                        matchSeries.getData().add(
                                new LineChart.Data<Number, Number>(sequence, matchPrice));
                    }

                    // remove points to keep us at no more than MAX_DATA_POINTS
                    if (matchSeries.getData().size() > MAX_DATA_SIZE) {
                        matchSeries.getData().remove(0,
                                matchSeries.getData().size() - MAX_DATA_SIZE);
                    }
                    if (buySeries.getData().size() > MAX_DATA_SIZE) {
                        buySeries.getData().remove(0, buySeries.getData().size() - MAX_DATA_SIZE);
                    }
                    if (sellSeries.getData().size() > MAX_DATA_SIZE) {
                        sellSeries.getData().remove(0, sellSeries.getData().size() - MAX_DATA_SIZE);
                    }

                    // update x bounds.
                    xAxis.setLowerBound(sequence - MAX_DATA_SIZE);
                    xAxis.setUpperBound(sequence - 1);
                }
            });
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            super.onWebSocketClose(statusCode, reason);
            System.out.println("Socket Closed: [" + statusCode + "] " + reason);
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            super.onWebSocketError(cause);
            cause.printStackTrace(System.err);
        }
    };
}
