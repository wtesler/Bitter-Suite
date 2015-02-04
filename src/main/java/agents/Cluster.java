package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class Cluster {

    final double[][] vectors;
    // final double[][] centroids;

    // Represents the quantized space surrounding each centroid.
    // Each queue holds all the vectors which are currently encapsulated by the
    // cell. The first element of the queue is the centroid.
    List<ArrayBlockingQueue<double[]>> voronoiCells;

    /**
     * @param vectors
     *            an array of normalized vectors
     * @param k
     *            k-means clustering
     */
    Cluster(final double[][] vectors, final int k) {
        this.vectors = vectors;
        // this.centroids = new double[k][vectors[0].length];
        this.voronoiCells = new ArrayList<ArrayBlockingQueue<double[]>>(k);

        initializeCentroids();
    }

    // Initialize centroids randomly, using Forgy method.
    void initializeCentroids() {

        // Create our own random list of numbers so that we don't encounter
        // duplicates like what is possible in Random.nextInt()
        int[] random = new int[vectors.length];
        Arrays.parallelPrefix(random, (left, right) -> {
            return left + 1;
        });
        shuffle(random);

        // Initialize our centroids to randomly selected vectors.
        for (int i = 0; i < voronoiCells.size(); i++) {
            voronoiCells.get(i).offer(vectors[random[i]]);
        }
    }

    /*
     * Part 1: assign a centroid to every vector.
     */
    void assign() {
        Arrays.stream(vectors).forEach(vector -> {
            double closestSoFar = Double.MIN_VALUE;
            int assignment = -1;
            for (int k = 0; k < voronoiCells.size(); k++) {
                // peek at the centroid
                double variance = LatentSourceModel.variance(vector, voronoiCells.get(k).peek());
                if (variance > closestSoFar) {
                    assignment = k;
                    closestSoFar = variance;
                }
            }
            // assign a centroid to the vector
            voronoiCells.get(assignment).offer(vector);
        });
    }

    /*
     * find the average vector inside each centroid. Set it as the new centroid.
     */
    void update() {
        voronoiCells.parallelStream().forEach((queue) -> {
            double[] oldCentroid = queue.poll();
            double[] newCentroid = oldCentroid;
            int count = 1;
            while (!queue.isEmpty()) {
                double[] vec = queue.poll();
                for (int i = 0; i < vec.length; i++) {
                    newCentroid[i] += vec[i];
                }
                count++;
            }
            final int denom = count;
            Arrays.parallelSetAll(newCentroid, index -> {
                return newCentroid[index] / denom;
            });

            // Add the new centroid to the now empty queue.
                queue.offer(newCentroid);
            });

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

    public static void main(String[] args) {
    }

}
