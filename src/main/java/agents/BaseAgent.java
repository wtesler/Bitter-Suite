package agents;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public abstract class BaseAgent {

    protected double myUSD, myBTC;

    protected int myWindowSize;

    /*
     * These deques are representative of fixed length windows. Imagine that
     * each one is placed on a data timeline and scan it from left to right.
     */

    // Correspond to the MATCH type messages
    protected final Deque<Double> matches;
    // Correspond to the RECEIVE type messages
    protected final Deque<Double> buys;
    protected final Deque<Double> sells;
    // Correspond to the OPEN type messages
    protected final Deque<Double> opens;
    // Correspond to the DONE type messages
    protected final Deque<Double> cancelled;
    protected final Deque<Double> completed;

    public BaseAgent(double initialUSD, double initialBTC, int windowSize) {
        myUSD = initialUSD;
        myBTC = initialBTC;

        myWindowSize = windowSize;

        // Correspond to the MATCH type messages
        matches = new ArrayDeque<>(myWindowSize);
        // Correspond to the RECEIVE type messages
        buys = new ArrayDeque<>(myWindowSize);
        sells = new ArrayDeque<>(myWindowSize);
        // Correspond to the OPEN type messages
        opens = new ArrayDeque<>(myWindowSize);
        // Correspond to the DONE type messages
        cancelled = new ArrayDeque<>(myWindowSize);
        completed = new ArrayDeque<>(myWindowSize);
    }

    public final int getWindowSize() {
        return myWindowSize;
    }

    protected void incrementWindow(Deque<Double> window, double value) {
        if (window.size() == getWindowSize()) {
            window.removeFirst();
        }
        window.offerLast(value);
    }

    public abstract WebSocketAdapter getListener();

    protected abstract void makeDecision();
}
