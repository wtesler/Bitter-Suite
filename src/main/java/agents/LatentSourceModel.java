package agents;

import java.util.Arrays;

public class LatentSourceModel {

    /**
     * @param curPattern
     *            the current pattern we are analyzing.
     * @param knownPatterns
     *            all the relevant known patterns we have learnt. Each one
     *            should be the length of curPattern.
     * @param knownPriceChanges
     *            all the relevant price changes associated with each known
     *            pattern. These values are found by taking the difference in
     *            price between the last y in the known pattern and the y2 in a
     *            few seconds after y. Should be the same length as patterns.
     * @return
     */
    public static double estimateFuturePrice(final double[] curPattern,
            final double[][] knownPatterns, final double[] knownPriceChanges, final double learntC) {
        double[] similarities = calculateSimilarities(curPattern, knownPatterns, learntC);
        double simSum = Arrays.stream(similarities).sum();
        double yHat = 0;
        for (int i = 0; i < knownPatterns.length; i++) {
            double similarityWeight = similarities[i] / simSum;
            yHat += similarityWeight * knownPriceChanges[i];
        }
        return yHat;
    }

    /**
     * This is part of Shah and Zhang's Equation 7. It returns the similarity
     * between the current pattern and all of the other known patterns
     *
     * @param curPattern
     *            the current pattern we are examining.
     * @param knownPatterns
     *            all the relevant known patterns we have learnt. Each one
     *            should be the same length as curPattern.
     * @param c
     *            A learnt constant value we use to optimize our prediction.
     * @return A vector of similarity comparisons between curPattern and
     *         knownPatterns.
     */
    private static double[] calculateSimilarities(final double[] curPattern,
            final double[][] knownPatterns, final double c) {
        double[] similarities = new double[knownPatterns.length];
        for (int i = 0; i < knownPatterns.length; i++) {
            similarities[i] = Math.exp(c * similarity(curPattern, knownPatterns[i]));
        }
        return similarities;
    }

    /**
     * @param vec1
     *            a normalized (mean=0, std=1) vector.
     * @param vec2
     *            a normalized (mean=0, std=1) vector.
     * @return a similarity rating from -1 to 1.
     */
    public static double similarity(final double[] vec1, final double[] vec2) {
        double sum = 0;
        for (int i = 0; i < vec1.length; i++) {
            sum += vec1[i] * vec2[i];
        }
        double denom = vec1.length - 1;
        // System.out.println("Similarity is: " + sum / denom);
        return sum / denom;
    }

    public static void normalizeVector(double[] vec) {
        double mean = mean(vec);
        double std = std(vec, mean);
        for (int i = 0; i < vec.length; i++) {
            vec[i] = (vec[i] - mean) / std;
        }
    }

    /**
     * @param vector
     * @return the mean of values in the vector.
     */
    public static double mean(final double[] vector) {
        return Arrays.stream(vector).average().getAsDouble();
    }

    /**
     * @param vector
     * @param mean
     *            the vector's mean
     * @return the standard deviation of this vector from the mean.
     */
    public static double std(final double[] vector, final double mean) {
        double std = 0;
        for (double value : vector) {
            std += Math.pow(value - mean, 2);
        }
        return Math.sqrt(std / (vector.length - 1));
    }
}
