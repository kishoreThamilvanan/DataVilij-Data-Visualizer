package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import settings.AppPropertyTypes;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

/**
 * The data files used by this data visualization applications follow a
 * tab-separated format, where each data point is named, labeled, and has a
 * specific location in the 2-dimensional X-Y plane. This class handles the
 * parsing and processing of such data. It also handles exporting the data to a
 * 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's
 * <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = new ApplicationTemplate().manager.getPropertyValue(AppPropertyTypes.NAME_ERROR.name());

        //'%s'.
        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'."
                    + NAME_ERROR_MSG, name));
        }
    }

    private Map<String, String> dataLabels;
    private Map<String, Point2D> dataPoints;
    private static boolean errorInData;
    private String data;
    private double minX, maxX;

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        data = "";
    }

    public boolean isErrorInData() {
        return TSDProcessor.errorInData;
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the
     * <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        ApplicationTemplate a = new ApplicationTemplate();
        PropertyManager manager = a.manager;
        TSDProcessor.errorInData = false;
        data = tsdString;
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();

        String[] lines = tsdString.split("\n");
        String[][] namesAndPoints = new String[lines.length][3];
        int flag = -1;
        String duplicateString = "";

        for (int i = 0; i < (lines.length); i++) {
            namesAndPoints[i] = lines[i].split("\t");
        }

        try {
            for (int i = 0; i < (lines.length); i++) {
                for (int j = i + 1; j < lines.length; j++) {

                    if (namesAndPoints[i][0].compareTo(namesAndPoints[j][0]) == 0) {
                        flag = j;
                        duplicateString = namesAndPoints[i][0];
                        break;
                    }
                }
            }

        } catch (Exception e) {

            clear();
            errorMessage.setLength(0);
            errorMessage.append(e.getClass().getSimpleName()).append(": ").
                    append(e.getMessage());
            hadAnError.set(true);
            TSDProcessor.errorInData = true;

        }

        if (flag != -1) {
            errorMessage.append(manager.getPropertyValue(AppPropertyTypes.DISTINCT_POINT_ERROR.name()))
                    .append("\n'")
                    .append(duplicateString)
                    .append("'")
                    .append("\t\t@line ")
                    .append(flag + 1);
            clear();
            hadAnError.set(true);
            TSDProcessor.errorInData = true;
        } else {

            Stream.of(tsdString.split("\n"))
                    .map(line -> Arrays.asList(line.split("\t")))
                    .forEach(list -> {
                        try {

                            String name = checkedname(list.get(0));
                            String label = list.get(1);
                            String[] pair = list.get(2).split(",");
                            Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));

                            dataLabels.put(name, label);
                            dataPoints.put(name, point);

                        } catch (Exception e) {

                            clear();
                            errorMessage.setLength(0);
                            errorMessage.append(e.getClass().getSimpleName()).append(": \n").
                                    append(e.getMessage()).
                                    append("\n@line ").
                                    append(dataLabels.size());
                            hadAnError.set(true);
                            TSDProcessor.errorInData = true;

                        }
                    });
        }

        if (errorMessage.length() > 0 && hadAnError.get()) {
            ErrorDialog error = ErrorDialog.getDialog();
            error.show("INVALID DATA", (errorMessage).toString());
        }

        if (lines.length > 10) {

            ErrorDialog error = ErrorDialog.getDialog();
            error.show("ATTENTION!", "Loaded data consists of "
                    + lines.length + manager.getPropertyValue(AppPropertyTypes.TEN_LINE_DIALOG.name()));

        }

    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        minX=0;
        maxX=0;
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
                if (minX == 0 || minX > point.getX()) {
                    minX = point.getX();
                }
                if (maxX < point.getX()) {
                    maxX = point.getX();
                }
            });

            series.setData(series.getData().sorted());
            chart.getData().add(series);
        }

        String[] lines = data.split("[\n\t]");

        int i = 0;

        for (final Series<Number, Number> series : chart.getData()) {
            for (final Data<Number, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip();
                tooltip.setText(lines[3 * i] + "");
                Tooltip.install(data.getNode(), tooltip);
                i++;
            }
        }


        for (int k = 0; k < chart.getData().size(); k++) {
            chart.getData().get(k).getNode().setStyle("-fx-stroke: transparent;");
        }

    }

    public void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@")) {
            throw new InvalidDataNameException(name);
        }
        return name;
    }
}
