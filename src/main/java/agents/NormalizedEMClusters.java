package agents;

public class NormalizedEMClusters extends EMClusters {

    public NormalizedEMClusters(double[][] featuresList, int k) {
        super(featuresList, k);
    }

    /**
     * The equation for <b>cosine similarity</b> Since both patterns are assumed
     * to be normalized as unit vectors, we do not need to include mean and std
     * in the calculations.
     *
     * @see <a href= "http://en.wikipedia.org/wiki/Cosine_similarity" >Wikipedia
     *      Article</a
     * @param pattern1
     *            a <b>normalized</b> (mean=0, std=1) vector.
     * @param pattern2
     *            a <b>normalized</b> (mean=0, std=1) vector.
     * @return a similarity rating from <b>-1 to 1.</b>
     */
    public static double difference(double[] feature1, double[] feature2) {
        double sum = 0;
        double mean1 = mean(feature1);
        double mean2 = mean(feature2);
        for (int i = 0; i < feature1.length; i++) {

            /*
             * The calculation here is a simple dot product. Very
             * computationally easy as long as mean=0 and std=1. That is why the
             * patterns must be normalized with normalizeVector() before being
             * passed into this function.
             */
            sum += (feature1[i] - mean1) * (feature2[i] - mean2);
        }

        sum /= feature1.length;
        sum /= variance(feature2, mean2);
        sum /= variance(feature1, mean1);

        // return the distance
        return sum;
    }

    /**
     * Takes any arbitrary pattern and normalizes it in place. Normalize means
     * the pattern revolves around a mean of 0 and a variance of 1. Note that
     * this method affects its input directly.
     *
     * @param pattern
     *            a sequence of double representing a time-series window.
     */
    public static double[] normalizeVector(double[] features) {

        // Find mean of features.
        double mean = mean(features);

        // Find variance of features
        double std = Math.sqrt(variance(features, mean));

        // System.out.format("before; mean: %f variance %f\n", mean, std);

        // Normalize to a mean of 0 and a variance of 1
        for (int i = 0; i < features.length; i++) {
            features[i] = (features[i] - mean) / std;
        }

        // System.out.format("after; mean: %f variance %f\n", mean(features),
        // std(features, mean(features)));
        return features;
    }

    private static double mean(double[] features) {
        // Find mean of features.
        double mean = 0;
        for (int i = 0; i < features.length; i++) {
            mean += features[i];
        }
        mean /= features.length;
        return mean;
    }

    private static double variance(double[] features, double mean) {
        // Find variance of features around mean.
        double variance = 0;
        for (int i = 0; i < features.length; i++) {
            variance += (features[i] - mean) * (features[i] - mean);
        }
        variance /= features.length;
        return variance;
    }

}
