package agents;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

public class SimpleClusters extends EMClusters {

    public SimpleClusters(double[][] featuresList, int k) {
        super(featuresList, k);
    }

    /*
     * 1 is same. -1 is very different. 0 is no correlation.
     */
    @Override
    public double difference(double[] features1, double[] features2) {
        double distance = features1.length;
        for (int i = 0; i < features1.length; i++) {
            double diff = features1[i] - features2[i];
            distance -= 2 * diff;
        }
        return distance / features1.length;
    }

    /**
     * @param features
     * @param centroid
     * @return e^(difference^2) is how we efficiently calculate distance.
     */
    @Override
    protected double probabilityDensity(double[] features, double[] centroid) {
        return Math.pow(Math.exp(difference(features, centroid)), 2);
    }

    /**
     * Scale the features so that they all fall between 0 and 1
     *
     * @param features
     */
    public static void scale(double[] features) {
        DoubleSummaryStatistics stats = Arrays.stream(features).summaryStatistics();
        for (int i = 0; i < features.length; i++) {
            features[i] -= stats.getMin();
            features[i] /= stats.getMax() - stats.getMin();
        }
    }

    /**
     * Calculates the similarity between every feature vector and every
     * centroid. Stores the values in a memo for later use.
     *
     * @return false when there are no more changes to make (we have converged).
     */
    @Override
    protected boolean assign() {
        // becomes true when probability density value changes. This is an
        // indicator whether our memo has converged or not.
        boolean changed = false;
        // outer-scoped for efficiency but only used in the inner-scope.
        double[] probabilities = new double[centroids.length];
        for (int i = 0; i < featuresList.length; i++) {
            for (int j = 0; j < centroids.length; j++) {
                // Compute probability density value.
                probabilities[j] = probabilityDensity(featuresList[i], centroids[j]);
                if (!changed
                        && (probabilities[j] - memo[i][j] > ALLOWED_ERROR || probabilities[j]
                                - memo[i][j] < -ALLOWED_ERROR)) {
                    // Something has changed.
                    changed = true;
                }

                memo[i][j] = probabilities[j];
            }
        }
        return changed;
    }

}
