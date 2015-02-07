package agents;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EMClusters {

    /**
     * The array of features we are analyzing
     */
    private double[][] featuresList;

    /**
     * The centroids we are discovering.
     */
    private double[][] centroids;

    /**
     * Lookup table that stores precomputed probability density values for
     * features.
     */
    private double[][] memo;

    /**
     * How much error are we willing to accept (used for detecting convergence)
     */
    private static final double ALLOWED_ERROR = 0.00000001;

    /**
     * @param featuresList
     *            A list of feature vectors
     * @param k
     *            how many clusters you want.
     */
    public EMClusters(final double[][] featuresList, final int k) {

        // All the features.
        this.featuresList = featuresList;

        // Hold Probability density values for features -> centroids
        this.memo = new double[featuresList.length][k];

        // A subset of features used as centroids. (Wikipedia: Voronoi
        // Tesselations)
        this.centroids = new double[k][featuresList[0].length];

        // Sets the initial features of the k centroids.
        initializeCentroids();

    }

    int i = 0;

    final public void run() {
        while (true) {
            // Assign probability density values to the memo.
            if (!assign()) {
                break;
            }
            // Adjust centroids according to the memo.
            update();
        }
    }

    /**
     * Assigns probability density values to every features to centroids
     * pairing.
     *
     * @return <b>false</b> if the memo has converged (meaning nothing has
     *         changed). This indicates to the {@link #run() run()} method that
     *         it should stop running (having achieved convergence).
     */
    final private boolean assign() {
        // becomes true when probability density value changes. This is an
        // indicator whether our memo has converged or not.
        boolean changed = false;
        // outer-scoped for efficiency but only used in the inner-scope.
        double[] probabilities = new double[centroids.length];
        for (int i = 0; i < featuresList.length; i++) {
            double sum = 0;
            for (int j = 0; j < centroids.length; j++) {
                // Compute probability density value.
                probabilities[j] = probabilityDensity(featuresList[i], centroids[j]);
                sum += probabilities[j];
            }

            // Normalize Probabilities and check for changes.
            for (int j = 0; j < centroids.length; j++) {
                // Divide by sum to normalize.
                probabilities[j] /= sum;
                if (!changed
                        && (probabilities[j] - memo[i][j] > ALLOWED_ERROR || probabilities[j]
                                - memo[i][j] < -ALLOWED_ERROR)) {
                    // Something has changed.
                    changed = true;
                }
                // Store the probability density in our memo so that it can be
                // used by our agents.
                    memo[i][j] = probabilities[j];
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
                sum += memo[j][i];
                // Go through each value in the centroid.
                for (int k = 0; k < featuresList[0].length; k++) {
                    // Each value in the centroid is a summation of each
                    // feature's value multiplied by the probability of seeing
                    // that feature.
                    centroids[i][k] += featuresList[j][k] * memo[j][i];
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

    // show another class the centroids, but don't let them modify it.
    final public List<double[]> getCentroids() {
        return Collections.unmodifiableList(Arrays.asList(centroids));
    }

    // Show another class our memo.
    final public double[][] getMemo() {
        return this.memo;
    }

    /**
     * Computes a simplified probability density function (PDF) known well in
     * statistics. The modifications are that we do not include a Z constant
     * because we will be normalizing the values anyways. Also that the distance
     * function is not necessarily Euclidian distance (L2-norm).
     *
     * @return probability density value.
     */
    final double probabilityDensity(double[] features, double[] centroid) {
        double similarity = distanceSquared(features, centroid);
        /*
         * We DO NOT include the Z value 1/sqrt(2pi) in our calculations because
         * this value will normalize anyways.
         */
        return Math.exp(similarity);
    }

    // Implementing Fisher–Yates simple shuffle for pseudo-randomness
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
     * Default is that Euclidian Distance IS used to measure similarity between
     * features. Use subclass NormalizedEMClusters if you want to use Shah and
     * Zhang's fast similarity algorithm (which requires normalized features).
     *
     * @param feature
     *            vector
     * @param feature
     *            vector
     * @return
     */
    public double distanceSquared(double[] feature1, double[] feature2) {
        int sum = 0;
        for (int i = 0; i < feature1.length; i++) {
            sum += (feature1[i] - feature2[i]) * (feature1[i] - feature2[i]);
        }
        // No need to sqrt because the PDF has the form distance^2 anyways.
        return sum;
    }

}
