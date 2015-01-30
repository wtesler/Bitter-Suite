package application;

public class Constants {

    public final static int MAX_DATA_SIZE = 500;

    // Coinbase API Metrics.
    public final static String MATCH_PRICE = "Match Price";
    public final static String RECEIVED_BUY = "Buy Request";
    public final static String RECEIVED_SELL = "Sell Request";

    // Coinbase API Message types.
    public final static String MATCH = "match";
    public final static String ERROR = "error";
    public final static String RECEIVED = "received";
    public final static String BUY = "buy";
    public final static String SELL = "sell";

    // Coinbase API Message keys.
    public final static String SEQUENCE = "sequence";
    public final static String MESSAGE = "message";
    public final static String PRICE = "price";
    public final static String TYPE = "type";
    public final static String SIDE = "side";
}
