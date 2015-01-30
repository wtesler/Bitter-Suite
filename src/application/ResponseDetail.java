package application;

/**
 * Used to indicate how much level of detail we want when querying Coinbase's
 * orderbook
 */
public enum ResponseDetail {
    ONLYBEST, TOP50, FULLORDER
}
