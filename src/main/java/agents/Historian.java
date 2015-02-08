package agents;

import java.util.ArrayList;
import java.util.List;

public class Historian {

    // Holds historic bitcoin price records.
    double[][] timeline;

    public Historian(double[][] timeline) {
        this.timeline = timeline;
    }

    /**
     * Scans across the history, segmenting the data into features of length
     * windowSize. Note, this is a "latent source" scan, meaning if history goes
     * from [0,100), then we will return history.length - windowSize amount of
     * features such that the first feature goes from [0,windowSize), the second
     * goes from [1, windowSize+1), the third from [2, windowSize+2) etc... <br/>
     * <br/>
     * This method is also recording volumes and labels for later use.
     *
     * @param windowSize
     *            the size of our scanning window
     * @param type
     *            an enum representing whether we want get only BUY data or SELL
     *            data or both.
     * @return a history containing the desired features / labels / volumes from
     *         the timeline.
     */
    public History extractOrders(int windowSize, TypeFilter type, SetFilter set) {
        List<double[]> featuresList = new ArrayList<double[]>();
        List<Double> labels = new ArrayList<Double>();
        List<Double> volumes = new ArrayList<Double>();

        // set bounds for the operation. This is used to separate Training and
        // Testing sets.
        int left =
                (set.equals(SetFilter.BOTH) || set.equals(SetFilter.TRAINING)) ? 0
                        : timeline.length / 2;
        int right =
                (set.equals(SetFilter.BOTH) || set.equals(SetFilter.TESTING)) ? timeline.length
                        : timeline.length / 2;

        // Scan from the left to the right.
        for (int i = left; i < right - windowSize; i++) {

            // Extract the label
            double label;
            if (i + windowSize >= timeline.length) {
                label = timeline[timeline.length - 1][History.CLOSE];
            } else {
                label =
                        timeline[i + windowSize][History.CLOSE]
                                - timeline[i + windowSize - 1][History.CLOSE];
            }

            // Filter out the unwanted orders based on the TypeFilter.
            if ((type.equals(TypeFilter.BOTH)) || (type.equals(TypeFilter.BUYS) && label > 0)
                    || (type.equals(TypeFilter.SELLS) && label < 0)) {
                double[] features = new double[windowSize];
                double volume = 0;
                for (int j = i; j < i + windowSize; j++) {
                    features[j - i] = timeline[j][History.CLOSE];
                    volume += timeline[j][History.VOLUME];
                }
                // Add the discovered feature to our lists.
                featuresList.add(features);
                labels.add(label);
                volumes.add(volume);
            }
        }
        // return a history containing the desired features / labels / volumes.
        return new History(featuresList, labels, volumes);
    }

    public class History {
        public static final int TIME = 0;
        public static final int LOW = 1;
        public static final int HIGH = 2;
        public static final int OPEN = 3;
        public static final int CLOSE = 4;
        public static final int VOLUME = 5;

        public double[][] features;
        public double[] labels;
        public double[] volumes;

        History(List<double[]> featuresList, List<Double> l, List<Double> v) {
            features =
                    featuresList
                            .toArray(new double[featuresList.size()][featuresList.get(0).length]);
            labels = convertListToDoubleArray(l);
            volumes = convertListToDoubleArray(v);
        }
    }

    public enum TypeFilter {
        BOTH, BUYS, SELLS;
    }

    public enum SetFilter {
        BOTH, TRAINING, TESTING;
    }

    private static double[] convertListToDoubleArray(List<Double> list) {
        Double[] doubles = list.toArray(new Double[list.size()]);
        double[] ret = new double[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = doubles[i].doubleValue();
        }
        return ret;
    }
}
