package agents;

public class Historian {

    // Holds historic bitcoin price and volume records.
    double[][] history;

    // The price change 10 seconds from the end of a feature vector
    double[] labels;

    // The amount of bitcoin that was bought and sold in the window timeframe.
    double[] volumes;

    public Historian(double[][] history) {
        this.history = history;
        this.labels = new double[history.length];
        this.volumes = new double[history.length];
    }

    /**
     * Scans across the history, segmenting the data into features of length
     * windowSize. Note, this is a "latent source" scan, meaning if history goes
     * from [0,100), then we will return history.length - windowSize amount of
     * features such that the first feature goes from [0,windowSize), the second
     * goes from [1, windowSize+1), the third from [2, windowSize+2) etc... <br/>
     * <br/>
     * This method is also recording volumes and labels for
     * later use.
     *
     * @param windowSize
     *            the size of our scanning window
     * @return a list of feature vectors.
     */
    public double[][] extractFeaturesList(int windowSize) {
        double[][] featuresList = new double[history.length - windowSize][windowSize];
        for (int i = 0; i < history.length - windowSize; i++) {
            double[] features = new double[windowSize];
            for (int j = i; j < i + windowSize; j++) {
                features[j - i] = history[j][History.CLOSE.ordinal()];
                volumes[i] += history[j][History.VOLUME.ordinal()];
            }
            featuresList[i] = features;

            if (i + windowSize >= history.length) {
                labels[i] = history[history.length - 1][History.CLOSE.ordinal()];
            } else {
                labels[i] =
                        history[i + windowSize][History.CLOSE.ordinal()]
                                - history[i + windowSize - 1][History.CLOSE.ordinal()];
            }
        }
        return featuresList;
    }

    public double[] getVolumes() {
        return this.volumes;
    }

    public double[] getLabels() {
        return this.labels;
    }
}
