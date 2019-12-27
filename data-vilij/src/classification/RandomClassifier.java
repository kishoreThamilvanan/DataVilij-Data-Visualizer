package classification;

import algorithms.Classifier;
import data.DataSet;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private static XYChart.Series<Number, Number> newSeries = new XYChart.Series<Number, Number>();
    ;
    private final int maxIterations;
    private final int updateInterval;
    private ApplicationTemplate applicationTemplate;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;
    private double minY, maxY;
    public static AtomicInteger j = new AtomicInteger(1);

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

    public RandomClassifier(DataSet dataset,
            int maxIterations,
            int updateInterval,
            boolean tocontinue,
            ApplicationTemplate applicationTemplate) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.applicationTemplate = applicationTemplate;

    }

    public void createChartLine() {

        TSDProcessor processor = ((AppData) applicationTemplate.getDataComponent()).getProcessor();

        if (!((AppUI) applicationTemplate.getUIComponent()).getChart().getData().contains(newSeries)) {
            ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().add(newSeries);
            newSeries.getNode().getStyleClass().add("series-" + newSeries);
            newSeries.getData().addAll(new XYChart.Data(processor.getMinX(), minY), new XYChart.Data(processor.getMaxX(), maxY));

            newSeries.getData().get(0).getNode().setVisible(false);
            newSeries.getData().get(1).getNode().setVisible(false);
        }
        newSeries.getNode().setStyle("-fx-stroke: black");
    }

    public void updateYValues() {

        // equation is Ax + By + c = 0
        // y = -(c + A*x)/B
        TSDProcessor processor = ((AppData) applicationTemplate.getDataComponent()).getProcessor();

        minY = -(output.get(2) + output.get(0) * processor.getMinX()) / output.get(1);
        maxY = -(output.get(2) + output.get(0) * processor.getMaxX()) / output.get(1);

        //updating the new y values of the changing line onto the graph.
        newSeries.getData().get(0).setYValue(minY);
        newSeries.getData().get(1).setYValue(maxY);

    }

    @Override

    public void run() {
        ((AppUI) applicationTemplate.getUIComponent()).getChart().setAnimated(false);
        Platform.runLater(this::createChartLine);
        for (int i = 1; i <= maxIterations && tocontinue(); i += updateInterval) {
            ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);
            int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant = RAND.nextInt(11);

            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            Platform.runLater(this::updateYValues);
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                Platform.runLater(this::updateYValues);
                break;
            }

            try {
                Thread.sleep(1000);
                ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!tocontinue() && j.get() <= maxIterations) {

            ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant = RAND.nextInt(11);

            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            if (j.get() > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                Platform.runLater(this::updateYValues);
            } else {
                Platform.runLater(this::updateYValues);
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

}
