package agents;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EMClusters {

    private double[][] featuresList;
    private double[][] centroids;
    private double[][] table;

    /**
     * @param featuresList
     *            A list of <b>normalized</b> feature vectors
     * @param k
     *            how many clusters you want.
     */
    public EMClusters(final double[][] featuresList, final int k) {

        // All the features.
        this.featuresList = featuresList;

        // Hold Probability density values for features -> centroids
        this.table = new double[featuresList.length][k];

        // A subset of features used as centroids. (Wikipedia: Voronoi
        // Tesselations)
        this.centroids = new double[k][featuresList[0].length];

        initializeCentroids();

    }

    final public void run() {
        while (true) {
            if (!assign()) {
                break;
            }
            update();
        }
    }

    /**
     * Assigns probability density values to every features to centroids
     * pairing.
     *
     * @return
     */
    final private boolean assign() {
        boolean changed = false;
        // outer-scoped for efficiency but only used in the inner-scope.
        double[] probabilities = new double[centroids.length];
        for (int i = 0; i < featuresList.length; i++) {
            // Used for normalization
            double sum = 0;
            for (int j = 0; j < centroids.length; j++) {
                probabilities[j] = probabilityDensity(featuresList[i], centroids[j]);
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

    /**
     * Updates the positions of the centroids based on the probability density
     * calculations.
     */
    final private void update() {
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
            for (int j = 0; j < featuresList.length; j++) {
                // Add the features probability of being in this centroid to our
                // normalization sum.
                sum += table[j][i];
                // Go through each value in the centroid.
                for (int k = 0; k < centroids[0].length; k++) {
                    // Each value in the centroid is a summation of each
                    // feature's value multiplied by the probability of seeing
                    // that feature.
                    centroids[i][k] += featuresList[j][k] * table[j][i];
                }
            }
            // Normalize the centroid's values.
            for (int j = 0; j < centroids[0].length; j++) {
                centroids[i][j] /= sum;
            }
        }
    }

    // Initialize centroids randomly, using Forgy method.
    final private void initializeCentroids() {

        // Create our own random list of numbers so that we don't encounter
        // duplicates like we would with Random.nextInt()
        final int[] random = new int[featuresList.length];
        // Creates an array like {0,1, 2, .. }
        Arrays.parallelPrefix(random, (left, right) -> {
            return left + 1;
        });
        shuffle(random);

        // Initialize our centroids to randomly selected vectors.
        for (int i = 0; i < centroids.length; i++) {
            centroids[i] = featuresList[random[i]];
        }
    }

    // Initialize centroids with a given list of vectors
    final public void setCentroids(final double[][] centroids) {
        this.centroids = centroids;
    }

    final double probabilityDensity(double[] feature, double[] centroid) {
        double similarity = similarity(feature, centroid);
        /*
         * We DO NOT include the Z value 1/sqrt(2pi) in our calculations because
         * this value will normalize anyways.
         */
        return Math.exp(-0.5 * similarity);
    }

    final public List<double[]> getCentroids() {
        return Collections.unmodifiableList(Arrays.asList(centroids));
    }

    // Implementing Fisher–Yates shuffle
    final private static void shuffle(int[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    /**
     * Euclidian Distance is used to measure similarity between features. Use
     * subclass NormalizedEMClusters if you want to use Shah and Zhang's fast
     * similarity algorithm (which requires normalized features).
     *
     * @param feature
     *            vector
     * @param feature
     *            vector
     * @return
     */
    public double similarity(double[] feature1, double[] feature2) {
        int sum = 0;
        for (int i = 0; i < feature1.length; i++) {
            sum += Math.pow(feature1[i] - feature2[i], 2);
        }
        // No need to sqrt.
        return sum;
    }

}
