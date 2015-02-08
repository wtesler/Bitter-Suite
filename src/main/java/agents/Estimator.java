package agents;

public class Estimator {

    private Estimator() {
    };

    /**
     * @param featuresList
     *            the list of features we got from the historian
     * @param labels
     *            the labels (10-second future price) associated with each
     *            feature
     * @param volumes
     *            the volumes (amount transacted) during each features timespan.
     * @param memo
     *            a record of similarity measurements between every feature to
     *            every centroid.
     */
    public static double[] getConfidenceScores(double[][] memo) {
        double[] scores = new double[memo[0].length];
        for (int i = 0; i < memo.length; i++) {
            for (int j = 0; j < scores.length; j++) {
                scores[j] += memo[i][j];
            }
        }
        for (int i = 0; i < scores.length; i++) {
            scores[i] /= memo.length;
        }
        return scores;
    }

    public static double[] getCentroidLabels(double[] featureLabels, double[][] memo) {
        double[] centroidLabels = new double[memo[0].length];
        for (int i = 0; i < centroidLabels.length; i++) {
            // Used for normalization.
            double memoSum = 0;
            for (int j = 0; j < featureLabels.length; j++) {
                centroidLabels[i] += featureLabels[j] * memo[j][i];
                memoSum += memo[j][i];
            }
            // Normalize the centroid labels.
            centroidLabels[i] /= memoSum;
        }
        return centroidLabels;
    }
}
