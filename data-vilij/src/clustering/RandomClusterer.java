/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

import algorithms.Clusterer;
import classification.RandomClassifier;
import data.DataSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author Kishore Thamilvanan
 */
public class RandomClusterer extends Clusterer {

    private static final Random randomValue = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;
    private ApplicationTemplate applicationTemplate;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;
    public static AtomicInteger j = new AtomicInteger(1);
    public List<Point2D> centroids;

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

    public RandomClusterer(DataSet dataset,
            int maxIterations,
            int updateInterval,
            ApplicationTemplate applicationTemplate,
            boolean tocontinue,
            int numclusters) {
        super(numclusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.applicationTemplate = applicationTemplate;
        this.tocontinue = new AtomicBoolean(tocontinue);
      

    }

    @Override
    public void run() {
        ((AppUI) applicationTemplate.getUIComponent()).getChart().setAnimated(false);
        initializeCentroids();
       
        for (int i = 1; i <= maxIterations && tocontinue(); i+=updateInterval) {
            ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);
            assignLabels();
            Platform.runLater(this::clearChart);
            Platform.runLater(this::displayChart);

            try {
                Thread.sleep(1000);
                ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClusterer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!tocontinue() && j.get() <= maxIterations) {
            assignLabels();
            Platform.runLater(this::clearChart);
            Platform.runLater(this::displayChart);

            ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);

            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClusterer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            j.set(j.get() + updateInterval); // j+= updateinterval
            try {
                synchronized (this) {
                    ((AppUI) applicationTemplate.getUIComponent()).getRun().setDisable(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getIsAlogRunning().set(false);
                    ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);

                    wait();
                    notifyAll();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ((AppUI) applicationTemplate.getUIComponent()).getRun().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getIsAlogRunning().set(false);
        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);

    }

    public void clearChart() {
        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
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

    private void assignLabels() {

        Set<String> series = new HashSet<>();
        List<String> labelNames = new ArrayList<>(dataset.getLabels().keySet());

        while (numberOfClusters > series.size()) {

            int randomInteger = randomValue.nextInt(labelNames.size());
            while (series.contains(labelNames.get(randomInteger))) {
                randomInteger++;
            }
            series.add(labelNames.get(randomInteger));
        }

        dataset.getLocations().forEach((instanceName, location) -> {
            int randomInteger = randomValue.nextInt(numberOfClusters);
            dataset.getLabels().put(instanceName, Integer.toString(randomInteger));
        });
    }

}
