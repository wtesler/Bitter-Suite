package test;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import agents.PeerPressureAgent;
import coinbase.CoinbaseClient;
import coinbase.Constants;
import coinbase.ResponseDetail;

public class Tests {

    @Test
    public void getHistoricalData() {
        // Calendar start and end dates.
        Calendar cal = Calendar.getInstance();

        cal.set(2015, 0, 23, 0, 0, 0);
        Date startDate = cal.getTime();

        cal.clear();

        cal.set(2015, 0, 29, 0, 0, 0);
        Date endDate = cal.getTime();

        try {
            // Getting match data from Jan 23 - Jan 29
            CoinbaseClient.getHistoricalData(startDate, endDate, 1000);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getOrderbookSnapshot() {
        try {
            CoinbaseClient.getOrderbook(ResponseDetail.FULLORDER);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void attachAgentToSocket() {

        CoinbaseClient client = new CoinbaseClient();

        // Add a new agent to the client.
        PeerPressureAgent agent = new PeerPressureAgent(500, 4);
        client.addListener(agent.getListener());

        URI uri = URI.create(Constants.COINBASE_SOCKET_URL);
        try {
            client.openWebSocket(uri);
            client.closeSocket();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
