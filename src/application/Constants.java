package application;

public class Constants {

    public final static int MAX_DATA_SIZE = 500;

    public static final String COINBASE_API_ENDPOINT = "https://api.exchange.coinbase.com";

    public static final String COINBASE_SOCKET_URL = "wss://ws-feed.exchange.coinbase.com";

    // Coinbase API Metrics.
    public final static String MATCH_PRICE = "Match Price";
    public final static String RECEIVED_BUY = "Buy Request";
    public final static String RECEIVED_SELL = "Sell Request";

    // Coinbase API Message types.
    public final static String MATCH = "match";
    public final static String ERROR = "error";
    public final static String RECEIVED = "received";
    public static final String SUBSCRIBE = "subscribe";
    public static final String BITCOIN_USD = "BTC-USD";

    // Coinabse API Message sides.
    public final static String BUY = "buy";
    public final static String SELL = "sell";

    // Coinbase API Message keys.
    public static final String TYPE = "type";
    public static final String PRODUCT_ID = "product_id";
    public final static String SEQUENCE = "sequence";
    public final static String MESSAGE = "message";
    public final static String PRICE = "price";
    public final static String SIDE = "side";
}
