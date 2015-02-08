package agents;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

public class SimpleClusters extends EMClusters {

    // Keeps a record of which centroid is assigned to each feature. Useful for
    // convergence.
    private int[] assignments;

    // How many features got reassigned last round? Useful for convergence.
    private int pastReassignments = Integer.MAX_VALUE;

    public SimpleClusters(double[][] featuresList, int k) {
        super(featuresList, k);
        assignments = new int[featuresList.length];
        for (int i = 0; i < assignments.length; i++) {
            assignments[i] = -1;
        }
    }

    /**
     * Calculates how "different" two features are.<br/>
     * <br/>
     * 1 is same. -1 is very different. 0 is no correlation.
     *
     * @param features1
     *            a feature vector
     * @param features2
     *            a feature vector
     * @return
     */
    public static double difference(double[] features1, double[] features2) {
        double distance = features1.length;
        for (int i = 0; i < features1.length; i++) {
            double diff = Math.abs(features1[i] - features2[i]);
            distance -= diff;
        }
        return distance / features1.length;
    }

    /**
     *
     * @param features
     * @param centroid
     * @return e^(difference^2) is how we efficiently calculate distance.
     */
    static double probabilityDensity(double[] features, double[] centroid) {
        // return Math.pow(Math.exp(difference(features, centroid)), 2);
        return difference(features, centroid);
    }

    /**
     * Scale the features so that they all fall between 0 and 1
     *
     * @param features
     */
    public static void scale(double[] features) {
        DoubleSummaryStatistics stats = Arrays.stream(features).summaryStatistics();
        if (stats.getMin() == stats.getMax()) {
            for (int i = 0; i < features.length; i++) {
                features[i] = 0;
            }
        } else {
            for (int i = 0; i < features.length; i++) {
                features[i] -= stats.getMin();
                features[i] /= stats.getMax() - stats.getMin();
            }
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
        int reassignments = 0;
        for (int i = 0; i < featuresList.length; i++) {
            double[] probabilities = new double[centroids.length];
            int assignedCentroid = -1;
            double max = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < centroids.length; j++) {
                // Compute probability density value.
                probabilities[j] = probabilityDensity(featuresList[i], centroids[j]);

                if (probabilities[j] > max) {
                    assignedCentroid = j;
                    max = probabilities[j];
                }

                double learntW = .25;
                memo[i][j] = learntW * probabilities[j];
            }

            if (assignments[i] == -1 || assignments[i] != assignedCentroid) {
                reassignments++;
                assignments[i] = assignedCentroid;
            }
            // The assigned centroid gets a bonus
            memo[i][assignedCentroid] = 1;
        }
        //System.out.println(Arrays.toString(assignments));
        System.out.println("New Assignments: " + reassignments);
        if (reassignments >= pastReassignments || reassignments == 0) {
            // We've reached a local optima. We should halt the algorithm.
            return false;
        }
        pastReassignments = reassignments;
        return true;
    }

}
