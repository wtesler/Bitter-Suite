package gui;

import static java.lang.System.out;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import agents.Estimator;
import agents.Historian;
import agents.Historian.History;
import agents.Historian.SetFilter;
import agents.Historian.TypeFilter;
import agents.SimpleClusters;
import coinbase.CoinbaseClient;

public class CentroidLayoutController {
    @FXML
    Pane root;
    @FXML
    LineChart<Number, Number> lc_chart;
    @FXML
    Button bt_next;
    @FXML
    Button bt_prev;
    @FXML
    Label l_fitness;

    NumberAxis xAxis;

    LineChart.Series<Number, Number> centroidSeries;

    List<double[]> centroids;
    int position = 0;

    double[] confidence;

    public void initialize() {

        // Our X Axis
        xAxis = (NumberAxis) lc_chart.getXAxis();

        // The line representing buy requests.
        centroidSeries = new LineChart.Series<Number, Number>();
        centroidSeries.setName("Centroid");
        lc_chart.getData().add(centroidSeries);

        // Aesthetic preference.
        lc_chart.setCreateSymbols(false);

        getCentroids();

        bt_next.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (position != centroids.size() - 1) {

                    centroidSeries.getData().clear();

                    position++;
                    for (int i = 0; i < centroids.get(position).length; i++) {
                        centroidSeries.getData().add(
                                new LineChart.Data<Number, Number>(i, centroids.get(position)[i]));
                    }
                    l_fitness.setText(Double.toString(confidence[position]));
                }
            }
        });

        bt_prev.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (position != 0) {

                    centroidSeries.getData().clear();

                    position--;
                    for (int i = 0; i < centroids.get(position).length; i++) {
                        centroidSeries.getData().add(
                                new LineChart.Data<Number, Number>(i, centroids.get(position)[i]));
                    }
                    l_fitness.setText(Double.toString(confidence[position]));
                }
            }
        });
    }

    private  void getCentroids() {
        try {

            // Calendar start and end dates.
            Calendar cal = Calendar.getInstance();

            cal.set(2014, 1, 1, 0, 0, 0);
            Date startDate = cal.getTime();

            cal.clear();

            cal.set(2017, 1, 1, 0, 0, 0);
            Date endDate = cal.getTime();

            // Every 10 seconds.
            int granularity = 10;

            // Get all the data from the start date to the end date.
            // One minute timeframe.
            double[][] response = CoinbaseClient.getHistoricalData(startDate, endDate, granularity);

            // Let the historian organize the data.
            Historian historian = new Historian(response);

            // 90 is 15 minutes (comes from 6 per minute * 15 minutes)
            final int windowSize = 180;

            // Get the featuresList from the historian
            History history = historian.extractOrders(windowSize, TypeFilter.BUYS, SetFilter.BOTH);

            // Scale all the features so that every value lies between 0 and 1.
            // We can do this without loss of information because the historian
            // holds volume data for all features.
            for (double[] features : history.features) {
                SimpleClusters.scale(features);
            }

            // Cluster the data into 100 centroids.
            SimpleClusters clusterer = new SimpleClusters(history.features, 20);
            double[][] memo = clusterer.run();

            // Print the estimates
            confidence = Estimator.getConfidenceScores(memo);

            double[] centroidLabels = Estimator.getCentroidLabels(history.labels, memo);

            centroids = clusterer.getCentroids();
            for (int i = 0; i < centroids.get(position).length; i++) {
                centroidSeries.getData().add(
                        new LineChart.Data<Number, Number>(i, centroids.get(position)[i]));
            }
            l_fitness.setText(Double.toString(confidence[position]));

            for (int i = 0; i < confidence.length; i++) {
                out.println("{Centroid_" + i + ": goodness of fit: " + confidence[i] + " label: "
                        + centroidLabels[i] + "}");
            }

            out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
