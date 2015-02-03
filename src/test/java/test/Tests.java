package test;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import agents.LatentSourceModel;
import agents.PeerPressureAgent;
import coinbase.Coinbase;
import coinbase.CoinbaseClient;
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
            // Getting match data from Jan 23, 2015 - Jan 29, 2015
            CoinbaseClient.getHistoricalData(startDate, endDate, 2);
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

    @Test
    public void normalizeVector() {
        double[] patternA = { 5, 4, 3, 2, 1 };
        double[] patternB = { 1, 2, 3, 4, 100 };
        LatentSourceModel.normalizeVector(patternA);
        LatentSourceModel.normalizeVector(patternB);
        double mean1 = LatentSourceModel.mean(patternA);
        double mean2 = LatentSourceModel.mean(patternB);
        assertEquals(mean1, LatentSourceModel.mean(patternB), 0);
        assertEquals(LatentSourceModel.std(patternA, mean1),
                LatentSourceModel.std(patternB, mean2), 0);
    }

    @Test
    public void LatentSourceModel() {

        double[] pattern = { 1, 2, 3, 4, 5 };
        double[] samePattern = { 1, 2, 3, 4, 5 };
        double[] similarPattern = { 1, 2, 3, 4, 6 };
        double[] diffPattern = { 5, 4, 3, 2, 1 };
        LatentSourceModel.normalizeVector(pattern);
        LatentSourceModel.normalizeVector(samePattern);
        LatentSourceModel.normalizeVector(diffPattern);
        LatentSourceModel.normalizeVector(similarPattern);

        double[][] knownPatterns1 = { samePattern, similarPattern };
        double[] knownPriceChanges1 = { 0, 1 };
        double estimate1 =
                LatentSourceModel.estimateFuturePrice(pattern, knownPatterns1, knownPriceChanges1,
                        1);
        double[][] knownPatterns2 = { samePattern, diffPattern };
        double[] knownPriceChanges2 = { 0, 1 };
        double estimate2 =
                LatentSourceModel.estimateFuturePrice(pattern, knownPatterns2, knownPriceChanges2,
                        1);

        if (estimate1 < .4 || estimate1 > .5) {
            fail("Estimate 1 (Similar) of " + estimate1 + " does not seem correct");
        }
        if (estimate2 > .2) {
            fail("Estimate 2 (Different) of " + estimate2 + " does not seem correct");
        }
    }
}
