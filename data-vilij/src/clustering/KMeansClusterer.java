/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

/**
 *
 * @author Kishore Thamilvanan
 */
import algorithms.Clusterer;
import data.DataSet;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int maxIterations;
    private final int updateInterval;
    private static AtomicBoolean tocontinue = new AtomicBoolean();
    private ApplicationTemplate applicationTemplate;
    public static AtomicInteger j = new AtomicInteger(1);

    public KMeansClusterer(DataSet dataset,
            ApplicationTemplate applicationTemplate,
            int maxIterations,
            int updateInterval,
            boolean tocontinue,
            int numberOfClusters) {

        super(numberOfClusters);
        this.dataset = dataset;
        this.applicationTemplate = applicationTemplate;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue.set(tocontinue);
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    @Override
    public void run() {
        ((AppUI) applicationTemplate.getUIComponent()).getChart().setAnimated(false);
        initializeCentroids();
        int iteration = 0;
        while (iteration < maxIterations && tocontinue()) {
            ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);
            iteration += updateInterval;
            assignLabels();
            Platform.runLater(this::clearChart);
            Platform.runLater(this::displayChart);
            recomputeCentroids();

            try {
                Thread.sleep(1000);
                ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
            } catch (InterruptedException ex) {
                Logger.getLogger(KMeansClusterer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        if (!tocontinue() && j.get() <= maxIterations) {
            assignLabels();
            Platform.runLater(() -> {
                clearChart();
                displayChart();
            });
            recomputeCentroids();

            j.getAndAdd(updateInterval);
            try {
                synchronized (this) {
                    ((AppUI) applicationTemplate.getUIComponent()).getRun().setDisable(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getIsAlogRunning().set(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);

                    wait();
                    notifyAll();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(KMeansClusterer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ((AppUI) applicationTemplate.getUIComponent()).getRun().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getIsAlogRunning().set(false);
        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);

    }

    private void initializeCentroids() {
        Set<String> chosen = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random r = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i))) {
                ++i;
            }
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance = Double.MAX_VALUE;
            int minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    public void clearChart() {
        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
    }

    public void displayChart() {

        Set<String> labels = new HashSet<>(dataset.getLabels().values());

        for (String label : labels) {

            XYChart.Series<Number, Number> newSeries = new XYChart.Series<>();
            newSeries.setName(label);

            dataset.getLabels().entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataset.getLocations().get(entry.getKey());
                XYChart.Data<Number, Number> DataLabels = new XYChart.Data<>(point.getX(), point.getY());
                newSeries.getData().add(DataLabels);
            });
            ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().add(newSeries);
            newSeries.getNode().setStyle("-fx-stroke: transparent");
        }
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

}
