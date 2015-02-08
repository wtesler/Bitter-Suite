package test;

import static java.lang.System.out;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import agents.EMClusters;
import agents.Estimator;
import agents.Historian;
import agents.Historian.History;
import agents.Historian.SetFilter;
import agents.Historian.TypeFilter;
import agents.LatentSourceModel;
import agents.NormalizedEMClusters;
import agents.PeerPressureAgent;
import agents.SimpleClusters;
import agents.TestAgent;
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
        PeerPressureAgent agent = new PeerPressureAgent(500, 4, 2048);
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
        double[] patternA =
        { 5, 4, 3, 2, 1 };
        double[] patternB =
        { 1, 2, 3, 4, 100 };
        LatentSourceModel.normalizeVector(patternA);
        LatentSourceModel.normalizeVector(patternB);
        double mean1 = Arrays.stream(patternA).average().getAsDouble();
        double mean2 = Arrays.stream(patternB).average().getAsDouble();
        assertEquals(mean1, mean2, 0);
        assertEquals(LatentSourceModel.std(patternA, mean1),
                LatentSourceModel.std(patternB, mean2), 0);
    }

    @Test
    public void latentSourceModelEstimations() {

        double[] pattern =
        { 1, 2, 3, 4, 5 };
        double[] samePattern =
        { 1, 2, 3, 4, 5 };
        double[] similarPattern =
        { 1, 2, 3, 4, 6 };
        double[] diffPattern =
        { 5, 4, 3, 2, 1 };
        LatentSourceModel.normalizeVector(pattern);
        LatentSourceModel.normalizeVector(samePattern);
        LatentSourceModel.normalizeVector(diffPattern);
        LatentSourceModel.normalizeVector(similarPattern);

        double[][] knownPatterns1 =
        { samePattern, similarPattern };
        double[] knownPriceChanges1 =
        { 0, 1 };
        double estimate1 =
                LatentSourceModel.estimateFuturePrice(pattern, knownPatterns1, knownPriceChanges1,
                        1);
        double[][] knownPatterns2 =
        { samePattern, diffPattern };
        double[] knownPriceChanges2 =
        { 0, 1 };
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

    @Test
    public void NormalizeMethodsProduceSameResult() {
        double[] patternA =
        { 1, 21, 29, 4, 6 };
        double[] patternB =
        { 1, 21, 29, 4, 6 };
        LatentSourceModel.normalizeVector(patternA);
        LatentSourceModel.normalizeVectorParallel(patternB);
        assertArrayEquals(patternA, patternB, 0);

    }

    @Test
    public void Normalize_SerialVsParallel_StressTest() {

        // Warm up the JVM the old fashion way with some Discrete Log work.
        for (int i = 0; i < 10000; i++) {
            Math.log(i);
        }

        // Get a fresh slate.
        System.gc();

        long time = System.currentTimeMillis();

        // 100 thousand
        for (int i = 0; i < 100000; i++) {
            // randomly generated pattern
            double[] pattern =
            { i, i + i, i / 2, i % 2, i * i };
            LatentSourceModel.normalizeVector(pattern);
        }
        long serialTime = System.currentTimeMillis() - time;

        time = System.currentTimeMillis();

        // 100 thousand
        for (int i = 0; i < 100000; i++) {
            // randomly generated pattern
            double[] pattern =
            { i, i + i, i / 2, i % 2, i * i };
            LatentSourceModel.normalizeVectorParallel(pattern);
        }
        long parallelTime = System.currentTimeMillis() - time;

        // System.out.println(serialTime + " : " + parallelTime);
        if (parallelTime < serialTime) {
            fail("Time to start using a parallel normalize!");
        }
    }

    @Test
    public void Similarities_Serial_Vs_Parallel_StressTest() {

        // Warm up the JVM the old fashion way with some Discrete Log work.
        for (int i = 0; i < 10000; i++) {
            Math.log(i);
        }

        // Get a fresh slate.
        System.gc();

        long time = System.currentTimeMillis();

        // 100 thousand
        for (int i = 0; i < 100000; i++) {
            double[] ourPattern =
            { 5, 11, 1, 23, 27 };
            // randomly generated pattern
            double[] patternA =
            { i, i + i, i / 2, i % 2, i * i };
            double[] patternB =
            { i << 1, i ^ 256, i >> 2, i - 2, i + 2 };
            double[][] relevantPatterns =
            { patternA, patternB };
            /*
             * Calculate Similarities.
             */
            final int weight = 1;
            double[] similarities = new double[relevantPatterns.length];
            for (int j = 0; j < relevantPatterns.length; j++) {
                similarities[j] =
                        Math.exp(weight
                                * LatentSourceModel.variance(ourPattern, relevantPatterns[j]));
            }
        }
        long serialTime = System.currentTimeMillis() - time;

        time = System.currentTimeMillis();

        // 100 thousand
        for (int i = 0; i < 100000; i++) {
            double[] ourPattern =
            { 5, 11, 1, 23, 27 };
            // randomly generated pattern
            double[] patternA =
            { i, i + i, i / 2, i % 2, i * i };
            double[] patternB =
            { i << 1, i ^ 256, i >> 2, i - 2, i + 2 };
            double[][] relevantPatterns =
            { patternA, patternB };
            LatentSourceModel.calculateSimilaritiesParallel(ourPattern, relevantPatterns, 1);
        }
        long parallelTime = System.currentTimeMillis() - time;

        if (parallelTime < serialTime) {
            fail("Time to start using a parallel calculateSimilarities!");
        }
    }

    @Test
    public void EM_Cluster() {
        double[][] featuresList = new double[4][2];
        featuresList[0] = new double[]
        { 3, 6 };
        featuresList[1] = new double[]
        { 4, 5 };
        featuresList[2] = new double[]
        { 1, 3 };
        featuresList[3] = new double[]
        { 2, 1 };
        EMClusters clusterer = new EMClusters(featuresList, 2);

        // Change the centroids in the cluster (for testing purposes)
        double[][] centroids = new double[2][2];
        centroids[0] = new double[]
        { 1, 4 };
        centroids[1] = new double[]
        { 3, 4 };
        clusterer.setCentroids(centroids);
        double[][] memo = clusterer.run();

        // Estimate the expected value of bitcoin ten seconds into the future.
        System.out.println(Arrays.toString(Estimator.getConfidenceScores(memo)));
    }

    @Test
    public void EM_Cluster_Normalized() {
        double[][] vectors = new double[4][2];
        vectors[0] = NormalizedEMClusters.normalizeVector(new double[]
        { 3, 6 });
        vectors[1] = NormalizedEMClusters.normalizeVector(new double[]
        { 4, 5 });
        vectors[2] = NormalizedEMClusters.normalizeVector(new double[]
        { 1, 3 });
        vectors[3] = NormalizedEMClusters.normalizeVector(new double[]
        { 2, 1 });
        NormalizedEMClusters clusterer = new NormalizedEMClusters(vectors, 2);
        double[][] centroids = new double[2][2];
        centroids[0] = new double[]
        { 1, 4 };
        centroids[1] = new double[]
        { 3, 4 };
        clusterer.setCentroids(centroids);
        clusterer.run();
        List<double[]> solutionCentroids = clusterer.getCentroids();
        for (double[] centroid : solutionCentroids) {
            System.out.println(Arrays.toString(centroid));
        }
    }

    @Test
    public void getAllHistoricData() {
        // Calendar start and end dates.
        Calendar cal = Calendar.getInstance();

        // Some arbitrarily far away past
        cal.set(2014, 0, 1, 0, 0, 0);
        Date startDate = cal.getTime();

        cal.clear();

        // Some arbitrarily far away future
        cal.set(2020, 0, 1, 0, 0, 0);
        Date endDate = cal.getTime();

        try {
            double[][] response = CoinbaseClient.getHistoricalData(startDate, endDate, 10);
            out.println("Done");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testEntireNormalizedProcess() {
        // Calendar start and end dates.
        Calendar cal = Calendar.getInstance();

        // Some arbitrarily far away past
        cal.set(2014, 0, 1, 0, 0, 0);
        Date startDate = cal.getTime();

        cal.clear();

        // Some arbitrarily far away future
        cal.set(2020, 0, 1, 0, 0, 0);
        Date endDate = cal.getTime();

        try {
            double[][] response = CoinbaseClient.getHistoricalData(startDate, endDate, 1000);
            Historian historian = new Historian(response);

            // 6 samples a minute for 15 minutes.
            final int windowSize = 6 * 15;
            History history = historian.extractOrders(windowSize, TypeFilter.BOTH, SetFilter.BOTH);
            for (double[] features : history.features) {
                NormalizedEMClusters.normalizeVector(features);
            }
            NormalizedEMClusters clusterer = new NormalizedEMClusters(history.features, 100);
            double[][] memo = clusterer.run();

            double[] estimates = Estimator.getConfidenceScores(memo);
            out.println(Arrays.toString(estimates));

            out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void SimpleClustersEntireProcess() {
        try {

            // Calendar start and end dates.
            Calendar cal = Calendar.getInstance();

            cal.set(2014, 1, 1, 0, 0, 0);
            Date startDate = cal.getTime();

            cal.clear();

            cal.set(2017, 1, 1, 0, 0, 0);
            Date endDate = cal.getTime();

            // Every 10 seconds.
            int granularity = 10;

            // Get all the data from the start date to the end date.
            // One minute timeframe.
            double[][] response = CoinbaseClient.getHistoricalData(startDate, endDate, granularity);

            // Let the historian organize the data.
            Historian historian = new Historian(response);

            final int windowSize = 90;

            // Get the featuresList from the historian
            History history =
                    historian.extractOrders(windowSize, TypeFilter.SELLS, SetFilter.BOTH);

            // Scale all the features so that every value lies between 0 and 1.
            // We can do this without loss of information because the historian
            // holds volume data for all features.
            for (double[] features : history.features) {
                SimpleClusters.scale(features);
            }

            // Cluster the data into 100 centroids.
            SimpleClusters clusterer = new SimpleClusters(history.features, 100);
            double[][] memo = clusterer.run();

            // Print the estimates
            double[] confidence = Estimator.getConfidenceScores(memo);

            double[] centroidLabels = Estimator.getCentroidLabels(history.labels, memo);

            for (int i = 0; i < confidence.length; i++) {
                out.println("{Centroid_" + i + ": goodness of fit: " + confidence[i]
                        + " label: " + centroidLabels[i] + "}");
            }

            // TestAgent agent = new TestAgent(clusterer.getCentroids(),
            // confidence, history.labels);

            // agent.run(history.features);

            out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void thoroughSimpleClusterTest() {
        try {

            double[][] buyList =
            {
            { 1, 2, 3 },
            { 3, 2, 1 },
            { 3, 2, 1 } };

            double[][] sellList =
            {
            { 4, 3 },
            { 6, 1 },
            { 6, 7 } };

            for (double[] features : buyList) {
                SimpleClusters.scale(features);
            }

            SimpleClusters buyClusterer = new SimpleClusters(buyList, 2);
            double[][] buyMemo = buyClusterer.run();

            double[] buyConfidence = Estimator.getConfidenceScores(buyMemo);

            double[] featureLabels =
            { 0, .5, 1 };

            double[] centroidLabels = Estimator.getCentroidLabels(featureLabels, buyMemo);

            for (int i = 0; i < buyConfidence.length; i++) {
                out.println("{Centroid_" + i + ": goodness of fit: " + buyConfidence[i]
                        + " label: " + centroidLabels[i] + "}");
            }

            for (double[] features : sellList) {
                SimpleClusters.scale(features);
            }

            SimpleClusters sellClusterer = new SimpleClusters(sellList, 1);
            double[][] sellMemo = sellClusterer.run();

            double[] sellConfidence = Estimator.getConfidenceScores(sellMemo);

            for (int i = 0; i < sellConfidence.length; i++) {
                // out.println("Sell Centroid " + i + " confidence: " +
                // sellConfidence[i]);
            }

            out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
