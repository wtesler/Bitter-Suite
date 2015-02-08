package agents;

import java.util.List;

public class TestAgent {

    List<double[]> centroids;
    double[] estimates;
    double[] labels;

    double usd = 10000;
    double btc = 40;

    public TestAgent(List<double[]> centroids, double[] estimates, double[] labels) {
        this.centroids = centroids;
        this.estimates = estimates;
        this.labels = labels;
    }

    public void setInitialPosition(double usd, double btc) {
        this.usd = usd;
        this.btc = btc;
    }

    public void run(double[][] featuresList) {
        for (int i = 0; i < featuresList.length; i++) {
            double value = 0;
            for (int j = 0; j < centroids.size(); j++) {
                value +=
                        estimates[j]
                                * SimpleClusters.probabilityDensity(featuresList[i],
                                        centroids.get(j));
            }
            System.out.println("value: " + value + " label: " + labels[i]);
        }
    }

}
