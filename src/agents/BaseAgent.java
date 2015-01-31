package agents;

import java.util.LinkedList;

import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public abstract class BaseAgent {

    protected double myUSD, myBTC;

    // Correspond to the MATCH type messages
    protected LinkedList<Double> matches = new LinkedList<>();
    // Correspond to the RECEIVE type messages
    protected LinkedList<Double> buys = new LinkedList<>();
    protected LinkedList<Double> sells = new LinkedList<>();
    // Correspond to the OPEN type messages
    protected LinkedList<Double> opens = new LinkedList<>();
    // Correspond to the DONE type messages
    protected LinkedList<Double> cancelled = new LinkedList<>();
    protected LinkedList<Double> completed = new LinkedList<>();


    public BaseAgent(double initialUSD, double initialBTC) {
        myUSD = initialUSD;
        myBTC = initialBTC;
    }

    public abstract WebSocketAdapter getListener();

    protected abstract void makeDecision();
}
