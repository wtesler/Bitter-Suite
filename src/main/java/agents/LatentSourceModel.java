package agents;

import java.util.Arrays;

public class LatentSourceModel {

    /**
     * @param curPattern
     *            the current pattern we are analyzing.
     * @param relevantPatterns
     *            all the relevant known patterns we have learned through
     *            k-means clustering. Each one should be the length of
     *            curPattern.
     * @param relevantPriceChanges
     *            all the relevant price changes associated with each known
     *            pattern. These values are found by taking the difference in
     *            price between the last y at position x-final and x-final +
     *            nearFuture+-. Should be the same length as patterns.
     * @param weight
     *            a learned constant. The higher it becomes, the more we trust
     *            our model.
     * @return
     */
    public static double estimateFuturePrice(final double[] curPattern,
            final double[][] relevantPatterns, final double[] relevantPriceChanges,
            final double weight) {

        /*
         *  Calculate Similarities as described in Shah and Zhang's similarity definition
         */
        double[] similarities = new double[relevantPatterns.length];
        for (int i = 0; i < relevantPatterns.length; i++) {
            similarities[i] = Math.exp(weight * variance(curPattern, relevantPatterns[i]));
        }

        /*
         * Used for normalizing the similarities that we receive.
         */
        double similarityTotal = Arrays.stream(similarities).sum();

        /*
         * Each relevant pattern contributes to the estimate.
         * Similar patterns hold stronger sway in the decision.
         */
        double estimate = 0;
        for (int i = 0; i < relevantPatterns.length; i++) {
            double similarity = similarities[i] / similarityTotal;
            estimate += similarity * relevantPriceChanges[i];
        }
        return estimate;
    }

    /**
     * * Note: This is the parallel version of calculateSimilarities. Generally
     * slower on few cores. <br/>
     * <br/>
     * This is part of Shah and Zhang's Equation 7. It returns the similarities
     * between the current pattern and all of the other relevant patterns.
     * Useful in the proceeding operations.
     *
     * @param curPattern
     *            the current pattern we are examining.
     * @param relevantPatterns
     *            all the relevant known patterns we have learnt. Each one
     *            should be the same length as curPattern.
     * @param weight
     *            A learned constant value we use to optimize our prediction.
     * @return A vector of similarity comparisons between curPattern and
     *         knownPatterns.
     */
    public static double[] calculateSimilaritiesParallel(final double[] curPattern,
            final double[][] relevantPatterns, final double weight) {
        /*
         * This holds an array with similarity values that correspond to
         * relevant patterns.
         */
        double[] similarities = new double[relevantPatterns.length];
        Arrays.parallelSetAll(similarities, i -> {
            return Math.exp(weight * variance(curPattern, relevantPatterns[i]));
        });
        return similarities;
    }

    /**
     * The equation for <b>variance</b> Since both patterns are assumed to be
     * normalized, we do not need to include mean in the calculations.
     *
     * @param pattern1
     *            a normalized (mean=0, std=1) vector.
     * @param pattern2
     *            a normalized (mean=0, std=1) vector.
     * @return a similarity rating from <b>-1 to 1.</b>
     */
    public static double variance(final double[] pattern1, final double[] pattern2) {
        double sum = 0;
        for (int i = 0; i < pattern1.length; i++) {
            /*
             * The calculation here is a simple dot product. Very
             * computationally easy as long as mean=0 and std=1. That is why the
             * patterns must be normalized with normalizeVector() before being
             * passed into this function.
             */
            sum += pattern1[i] * pattern2[i];
        }

        // Why minus 1?
        double denom = pattern1.length - 1;

        // return the variance
        return sum / denom;
    }

    /**
     * Takes any arbitrary pattern and normalizes it in place. Normalize means
     * the pattern revolves around a mean of 0 and a standard deviation of 1.
     *
     * @param pattern
     *            a sequence of double representing a time-series window.
     */
    public static void normalizeVector(double[] pattern) {
        double mean = Arrays.stream(pattern).average().getAsDouble();
        double std = std(pattern, mean);
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = (pattern[i] - mean) / std;
        }
    }

    /**
     * Note: This is the parallel version of normalizeVector. This is much
     * slower when number of cores is small! <br/>
     * <br/>
     * Takes any arbitrary pattern and normalizes it in place. Normalize means
     * the pattern revolves around a mean of 0 and a standard deviation of 1.
     *
     * @param pattern
     *            a sequence of double representing a time-series window.
     */
    public static void normalizeVectorParallel(double[] pattern) {
        double mean = Arrays.stream(pattern).average().getAsDouble();
        double std = std(pattern, mean);
        Arrays.parallelSetAll(pattern, i -> {
            return (pattern[i] - mean) / std;
        });
    }

    public static double std(final double[] pattern, final double mean) {
        double std =
                Arrays.stream(pattern).map(p -> Math.pow(p - mean, 2)).reduce(0, (a, b) -> a + b);
        std = Math.sqrt(std / (pattern.length - 1));
        return std;
    }
}
