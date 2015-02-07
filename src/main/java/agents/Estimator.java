package agents;

public class Estimator {

    double[] estimates;

    public Estimator(double[][] featuresList, double[] labels, double[] volumes, double[][] memo) {
        estimates = new double[memo[0].length];
        estimate(featuresList, labels, volumes, memo);
    }

    /**
     * @param featuresList the list of features we got from the historian
     * @param labels the labels (10-second future price) associated with each feature
     * @param volumes the volumes (amount transacted) during each features timespan.
     * @param memo a record of similarity measurements between every feature to every centroid.
     */
    private void estimate(double[][] featuresList, double[] labels, double[] volumes,
            double[][] memo) {
        for (int i = 0; i < featuresList.length; i++) {
            for (int j = 0; j < estimates.length; j++) {
                // Our estimate is based off of 3 things...
                estimates[j] += labels[i] * volumes[i] * memo[i][j];
            }
        }

        // Scale the estimates so that they all fall between [0, 1)
        SimpleClusters.scale(estimates);
    }

    public double[] getEstimates() {
        return estimates;
    }

}
