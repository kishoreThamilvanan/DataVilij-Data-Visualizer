package data;

import javafx.geometry.Point2D;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import settings.AppPropertyTypes;
import vilij.components.ErrorDialog;
import vilij.templates.ApplicationTemplate;

/**
 * This class specifies how an algorithm will expect the dataset to be. It is
 * provided as a rudimentary structure only, and does not include many of the
 * sanity checks and other requirements of the use cases. As such, you can
 * completely write your own class to represent a set of data instances as long
 * as the algorithm can read from and write into two {@link java.util.Map}
 * objects representing the name-to-label map and the name-to-location (i.e.,
 * the x,y values) map. These two are the {@link DataSet#labels} and
 * {@link DataSet#locations} maps in this class.
 *
 * @author Ritwik Banerjee
 */
public class DataSet {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = new ApplicationTemplate().manager.getPropertyValue(AppPropertyTypes.NAME_ERROR.name());

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private static String nameFormatCheck(String name) {
        if (!name.startsWith("@")) {
            ErrorDialog error = ErrorDialog.getDialog();
            error.show("INVALID DATA", String.format("Invalid name '%s'."
                    + new ApplicationTemplate().manager.getPropertyValue(AppPropertyTypes.NAME_ERROR.name()), name));
        }
        return name;
    }

    private static Point2D locationOf(String locationString) {
        String[] coordinateStrings = locationString.trim().split(",");
        return new Point2D(Double.parseDouble(coordinateStrings[0]), Double.parseDouble(coordinateStrings[1]));
    }

    private Map<String, String> labels;
    private Map<String, Point2D> locations;

    /**
     * Creates an empty dataset.
     */
    public DataSet() {
        labels = new HashMap<>();
        locations = new HashMap<>();
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public Map<String, Point2D> getLocations() {
        return locations;
    }

    public void updateLabel(String instanceName, String newlabel) {
        if (labels.get(instanceName) == null) {
            throw new NoSuchElementException();
        }
        labels.put(instanceName, newlabel);
    }

    public void addInstance(String tsdLine) {
        String[] arr = tsdLine.split("\t");
        try {
            labels.put(nameFormatCheck(arr[0]), arr[1]);
            locations.put(arr[0], locationOf(arr[2]));
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, e);
        }
        
    }

    public static DataSet fromTSDFile(Path tsdFilePath) {
        DataSet dataset = new DataSet();
        try {
            Files.lines(tsdFilePath).forEach(line -> {
                dataset.addInstance(line);
            });
        } catch (NullPointerException e) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ArrayIndexOutOfBoundsException ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataset;
    }

    public void clear() {
        labels.clear();
        locations.clear();
    }
}
