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

    // Stores the value of the last match price recorded.
    private double matchPrice = 0;

    // Stores the value of the last buy request recorded.
    private double buyPrice = 0;

    // Stores the value of the last sell request recorded.
    private double sellPrice = 0;

    public void initialize() {

        // Our X Axis
        xAxis = (NumberAxis) lc_chart.getXAxis();

        // The line representing buy requests.
        buySeries = new LineChart.Series<Number, Number>();
        buySeries.setName(Constants.RECEIVED_BUY);
        lc_chart.getData().add(buySeries);

        // The line representing sell requests.
        sellSeries = new LineChart.Series<Number, Number>();
        sellSeries.setName(Constants.RECEIVED_SELL);
        lc_chart.getData().add(sellSeries);

        // The line representing our match price.
        matchSeries = new LineChart.Series<Number, Number>();
        matchSeries.setName(Constants.MATCH_PRICE);
        lc_chart.getData().add(matchSeries);

        // Aesthetic preference.
        lc_chart.setCreateSymbols(false);
    }

    public WebSocketAdapter getListener() {
        return new WebSocketAdapter() {

            @Override
            public void onWebSocketConnect(Session sess) {
                super.onWebSocketConnect(sess);
                System.out.println("Socket Connected: " + sess);
            }

            @Override
            public void onWebSocketText(String message) {
                super.onWebSocketText(message);

                JSONObject json = new JSONObject(message);
                // Parse the message that we've just received from Coinbase.
                String type = json.getString(Constants.TYPE);

                // Base case: Coinbase sent us an error.
                if (type.equals(Constants.ERROR)) {
                    System.err.println("Coinbase sent an error: "
                            + json.getString(Constants.MESSAGE));
                    return;
                }

                // Parameters
                long sequence = json.getLong(Constants.SEQUENCE);
                double price = json.getDouble(Constants.PRICE);
                String side = json.getString(Constants.SIDE);

                // Notify the agent about the new information.

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        // Update the chart depending on the type of message
                        // received.
                        if (type.equals(Constants.MATCH)) {
                            // Update the match price.
                            matchPrice = price;
                        } else if (type.equals(Constants.RECEIVED)) {

                            if (Constants.BUY.equals(side)) {
                                buyPrice = price;
                                if (buyPrice != 0) {
                                    // Update the buy price.
                                    buySeries.getData().add(
                                            new LineChart.Data<Number, Number>(sequence, buyPrice));
                                }
                            } else if (Constants.SELL.equals(side)) { // SELL
                                sellPrice = price;
                                if (sellPrice != 0) {
                                    // Update the sell price.
                                    sellSeries.getData()
                                            .add(new LineChart.Data<Number, Number>(sequence,
                                                    sellPrice));
                                }
                            } else {
                                System.err.println("Could not recognize side: " + side);
                            }
                        }

                        // Always update the matchPrice (if not 0)
                        if (matchPrice != 0) {
                            matchSeries.getData().add(
                                    new LineChart.Data<Number, Number>(sequence, matchPrice));
                        }

                        // remove points to keep us at no more than
                        // MAX_DATA_POINTS
                        if (matchSeries.getData().size() > Constants.MAX_DATA_SIZE) {
                            matchSeries.getData().remove(0,
                                    matchSeries.getData().size() - Constants.MAX_DATA_SIZE);
                        }
                        if (buySeries.getData().size() > Constants.MAX_DATA_SIZE) {
                            buySeries.getData().remove(0,
                                    buySeries.getData().size() - Constants.MAX_DATA_SIZE);
                        }
                        if (sellSeries.getData().size() > Constants.MAX_DATA_SIZE) {
                            sellSeries.getData().remove(0,
                                    sellSeries.getData().size() - Constants.MAX_DATA_SIZE);
                        }

                        // update x bounds.
                        xAxis.setLowerBound(sequence - Constants.MAX_DATA_SIZE);
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

}
