package agents;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EMClusters {

    double[][] features;
    double[][] centroids;
    double[][] table;

    public EMClusters(final double[][] features, final double[] labels, final int k)
            throws ClusterException {

        if (features.length != labels.length) {
            throw new ClusterException("Features and Labels must be the same length");
        } else if (features.length < k) {
            throw new ClusterException("you are requesting more clusters than there are features.");
        }

        this.features = features;

        // The probability density value that each features assigns to each
        // centroid.
        this.table = new double[features.length][k];

        // A subset of features
        this.centroids = new double[k][features[0].length];

        initializeCentroids();

    }

    public void run() {
        while (true) {
            if (!assign()) {
                break;
            }
            update();
        }
    }

    private boolean assign() {
        boolean changed = false;
        // outer-scoped for efficiency but only used in the inner-scope.
        double[] probabilities = new double[centroids.length];
        for (int i = 0; i < features.length; i++) {
            // Used for normalization
            double sum = 0;
            for (int j = 0; j < centroids.length; j++) {
                probabilities[j] = probabilityDensity(features[i], centroids[j]);
                sum += probabilities[j];
            }
            // Normalize Propabilities.
            for (int j = 0; j < centroids.length; j++) {
                probabilities[j] /= sum;
                if (!changed && probabilities[j] != table[i][j]) {
                    changed = true;
                }
                table[i][j] = probabilities[j];
            }
        }
        return changed;
    }

    private void update() {
        // Reset each centroids' values.
        for (int i = 0; i < centroids.length; i++) {
            for (int j = 0; j < centroids[0].length; j++) {
                centroids[i][j] = 0;
            }
        }
        // Go over each centroid
        for (int i = 0; i < centroids.length; i++) {
            // Used for normalization
            double sum = 0;
            // Go through each feature
            for (int j = 0; j < features.length; j++) {
                // Add the features probability of being in this centroid to our
                // normalization sum.
                sum += table[j][i];
                // Go through each value in the centroid.
                for (int k = 0; k < centroids[0].length; k++) {
                    // Each value in the centroid is a summation of each
                    // feature's value multiplied by the probability of seeing
                    // that feature.
                    centroids[i][k] += features[j][k] * table[j][i];
                }
            }
            // Normalize the centroid's values.
            for (int j = 0; j < centroids[0].length; j++) {
                centroids[i][j] /= sum;
            }
        }
    }

    // Initialize centroids randomly, using Forgy method.
    private void initializeCentroids() {

        // Create our own random list of numbers so that we don't encounter
        // duplicates like we would with Random.nextInt()
        final int[] random = new int[features.length];
        // Creates an array like {0,1, 2, .. }
        Arrays.parallelPrefix(random, (left, right) -> {
            return left + 1;
        });
        shuffle(random);

        // Initialize our centroids to randomly selected vectors.
        for (int i = 0; i < centroids.length; i++) {
            centroids[i] = features[random[i]];
        }
    }

    // Initialize centroids with a given list of vectors
    public void setCentroids(final double[][] centroids) {
        this.centroids = centroids;
    }

    double probabilityDensity(double[] feature, double[] centroid) {
        double distance = euclidianDistanceSquared(feature, centroid);
        /*
         * We DO NOT include the Z value 1/sqrt(2pi) in our calculations because
         * this value will normalize anyways.
         */
        return Math.exp(-0.5 * distance);
    }

    private double euclidianDistanceSquared(double[] feature, double[] centroid) {
        double sum = 0;
        for (int i = 0; i < feature.length; i++) {
            sum += Math.pow(feature[i] - centroid[i], 2);
        }

        /*
         * We DO NOT Sqrt the sum before returning it since the square wouldn't
         * change which centroid is nearest.
         */
        return sum;
    }

    public List<double[]> getCentroids() {
        return Collections.unmodifiableList(Arrays.asList(centroids));
    }

    // Implementing Fisher–Yates shuffle
    private static void shuffle(int[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public class ClusterException extends Exception {
        public ClusterException(String string) {
            super(string);
        }
    }
}
