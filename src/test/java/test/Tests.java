package test;

import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import agents.PeerPressureAgent;
import coinbase.Coinbase;
import coinbase.CoinbaseClient;
import coinbase.ResponseDetail;

public class Tests {

    @Test
    public void getHistoricalData() {
        // Calendar start and end dates.
        Calendar cal = Calendar.getInstance();

        cal.set(2014, 0, 23, 0, 0, 0);
        Date startDate = cal.getTime();

        cal.clear();

        cal.set(2016, 0, 29, 0, 0, 0);
        Date endDate = cal.getTime();

        try {
            // Getting match data from 2014 - 2016
            String response = CoinbaseClient.getHistoricalData(startDate, endDate, 2);
            System.out.println(response);
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

        URI uri = URI.create(Coinbase.COINBASE_SOCKET_URL);
        try {
            client.openWebSocket(uri);
            client.closeSocket();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
