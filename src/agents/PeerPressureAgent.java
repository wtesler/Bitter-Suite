package agents;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.json.JSONObject;

import coinbase.Coinbase;

public class PeerPressureAgent extends BaseAgent {

    // How many records we are willing to hold onto for any given message.
    public static final int MEMORY = 1000;

    public PeerPressureAgent(int initialUSD, int initialBTC) {
        super(initialUSD, initialBTC);
    }

    @Override
    public WebSocketAdapter getListener() {
        return new WebSocketAdapter() {
            @Override
            public void onWebSocketConnect(Session sess) {
                super.onWebSocketConnect(sess);
                System.out.println("Agent has connected");
            }

            @Override
            public void onWebSocketText(String message) {
                super.onWebSocketText(message);
                // RECEIVED A MESSAGE FROM THE SERVER.
                // THIS HAS ALL THE JUICY INFORMATION YOU COULD WANT.
                JSONObject json = new JSONObject(message);

                String type = json.getString(Coinbase.TYPE);

                // Base case: Coinbase sent us an error.
                if (type.equals(Coinbase.ERROR)) {
                    System.err.println("Coinbase sent an error: "
                            + json.getString(Coinbase.MESSAGE));
                    return;
                }

                // Parameters
                long sequence = json.getLong(Coinbase.SEQUENCE);
                double price = json.getDouble(Coinbase.PRICE);
                String side = json.getString(Coinbase.SIDE);

                if (price != 0) {
                    // Update the chart depending on the type of message
                    // received.
                    if (type.equals(Coinbase.MATCH)) {
                        if (matches.size() == 0) {
                            double total = myUSD + (price * myBTC);
                            System.out.format(
                                    "Starting with %f USD and %f BTC which totals %f USD\n", myUSD,
                                    myBTC, total);
                        }
                        matches.add(price);
                        if (matches.size() > MEMORY) {
                            matches.removeFirst();
                        }
                        makeDecision();
                    } else if (type.equals(Coinbase.RECEIVED)) {
                        if (Coinbase.BUY.equals(side)) {
                            buys.add(price);
                            if (buys.size() > MEMORY) {
                                buys.removeFirst();
                            }
                        } else if (Coinbase.SELL.equals(side)) {
                            sells.add(price);
                            if (sells.size() > MEMORY) {
                                sells.removeFirst();
                            }
                        } else {
                            System.err.println("Could not recognize side: " + side);
                        }
                    } else {
                        return;
                    }
                }
            }

            @Override
            public void onWebSocketClose(int statusCode, String reason) {
                super.onWebSocketClose(statusCode, reason);
                // CLOSED SOCKET
            }

            @Override
            public void onWebSocketError(Throwable cause) {
                super.onWebSocketError(cause);
                // ERROR OCCURRED
            }
        };
    }

    @Override
    protected void makeDecision() {

        if (matches.size() != 0 && sells.size() != 0 && buys.size() != 0) {

            double matchTotal = 0;
            for (double price : matches) {
                matchTotal += price;
            }
            double avgMatchPrice = matchTotal / matches.size();

            double buyTotal = 0;
            for (double price : buys) {
                buyTotal += price;
            }
            double avgBuyPrice = buyTotal / buys.size();

            double sellTotal = 0;
            for (double price : sells) {
                sellTotal += price;
            }
            double avgSellPrice = sellTotal / sells.size();

            if ((avgBuyPrice + avgSellPrice) / 2 > avgMatchPrice) {
                if (myBTC > .012) {
                    myBTC -= .012;
                    myUSD += (matches.getLast() * .012);
                }
            } else {
                if (myUSD > 3) {
                    myUSD -= 3;
                    myBTC += (3 / matches.getLast());
                }
            }

            double total = myUSD + (matches.getLast() * myBTC);
            System.out.format("%f USD %f BTC %f Total\n", myUSD, myBTC, total);
        }
    }
}
