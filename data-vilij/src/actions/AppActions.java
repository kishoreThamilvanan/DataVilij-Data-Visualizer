package actions;

import data.DataSet;
import dataprocessors.AppData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.imageio.ImageIO;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ConfirmationDialog;
import vilij.components.ConfirmationDialog.Option;
import vilij.propertymanager.PropertyManager;

/**
 * This is the concrete implementation of the action handlers required by the
 * application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /**
     * The application to which this class of actions belongs.
     */
    private ApplicationTemplate applicationTemplate;
    private String savedFilePath = "";

    public String getSavedFilePath() {
        return savedFilePath;
    }

    /**
     * Path to the data file currently active.
     */
    Path dataFilePath;

    public Path getDataFilePath() {
        return dataFilePath;
    }

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {

        if (((AppUI) (applicationTemplate.getUIComponent())).getTextArea().getText().length() > 0) {
            ConfirmationDialog dialogue = ConfirmationDialog.getDialog();
            dialogue.show("Warning!", applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));
            Option selectedOption = dialogue.getSelectedOption();

            if (selectedOption != null) {
                switch (selectedOption) {
                    case YES:
                        handleSaveRequest();
                        break;
                    default:
                        break;
                }

                if (selectedOption != Option.CANCEL) {
                    ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().clear();
                    ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().clear();
                    ((AppUI) (applicationTemplate.getUIComponent())).getChart().getData().clear();
                    savedFilePath = "";
                }
            }

        }
    }

    @Override
    public void handleSaveRequest() {

        PropertyManager manager = applicationTemplate.manager;

        AppData appData = (AppData) applicationTemplate.getDataComponent();

        appData.loadData(((AppUI) (applicationTemplate.getUIComponent())).getTextArea().getText());
        if (!appData.getProcessor().isErrorInData()) {

            if ((savedFilePath).equals("")) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.FILE_CHOOSER_TITLE.name()));
                fileChooser.getExtensionFilters().addAll(
                        new ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()),
                                manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name())));

                File selectedFile = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

                try {

                    if (selectedFile != null) {
                        FileWriter fileWriter = new FileWriter(selectedFile);
                        savedFilePath = selectedFile.getAbsolutePath();
                        dataFilePath = selectedFile.toPath();
                        fileWriter.write(((AppUI) (applicationTemplate.getUIComponent())).getTextArea().getText());
                        fileWriter.close();
                    }

                } catch (IOException ex) {
                    Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NullPointerException e) {
                    Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, e);
                }
            } else {

                FileWriter fileWriter;
                try {
                    fileWriter = new FileWriter(new File(savedFilePath));
                    fileWriter.write(((AppUI) (applicationTemplate.getUIComponent())).getTextArea().getText());
                    fileWriter.close();
                } catch (IOException ex) {
                    Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            // disables the save button after saved once.
            ((AppUI) (applicationTemplate.getUIComponent())).saveButtonDisable(true);
        }
    }

    @Override
    public void handleLoadRequest() {

        File selectedFile = new FileChooser().showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        String line = "", tenLines = "";
        ArrayList<String> restofLines = new ArrayList<String>();

        if (selectedFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String check;
                int i = 0;

                while ((check = reader.readLine()) != null) {
                    line += check + "\n";
                    if (i < 10) {
                        tenLines += check + "\n";
                    } else {
                        restofLines.add(check);
                    }
                    i++;
                }

            } catch (IOException ex) {
                Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ArrayIndexOutOfBoundsException ex) {
                Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
            }

            ((AppData) (applicationTemplate.getDataComponent())).getProcessor().clear();
            ((AppUI) (applicationTemplate.getUIComponent())).getChart().getData().clear();
            AppData appData = (AppData) applicationTemplate.getDataComponent();

            appData.loadData(line);
            ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().clear();
            if (!(appData.getProcessor().isErrorInData())) {
                appData.displayData();                          //loads all data read from the file
                savedFilePath = selectedFile.getAbsolutePath();
                dataFilePath = selectedFile.toPath();
                ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().setText(tenLines);
                ((AppUI) (applicationTemplate.getUIComponent())).saveButtonDisable(true);
                ((AppUI) (applicationTemplate.getUIComponent())).setDataSet(DataSet.fromTSDFile(dataFilePath));

                //ten lines thingy
                if (line.split("\n").length > 10) {

                    ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                            if (newValue.split("\n").length < 10 && !restofLines.isEmpty()) {
                                ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().setText(
                                        ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().getText()
                                        + restofLines.remove(0) + "\n");
                            }
                        }
                    });
                }
                ((AppUI) (applicationTemplate.getUIComponent())).getMetaDataLabel()
                        .setText(savedFilePath + ((AppUI) (applicationTemplate.getUIComponent())).metaData(line));
            }
            ((AppUI) (applicationTemplate.getUIComponent())).getTextArea().setDisable(true);
        }
    }

    @Override
    public void handleExitRequest() {

        if (((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText().length() > 0) {
            try {
                if (((AppUI) applicationTemplate.getUIComponent()).getIsAlogRunning().get()) {
                    ConfirmationDialog dialogue = ConfirmationDialog.getDialog();
                    dialogue.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.WARNING.name()),
                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.WARNING_TEXT.name()) + "\n"
                            + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.FORCE_CLOSE.name()));
                    Option selectedOption = dialogue.getSelectedOption();

                    if (selectedOption != null) {
                        switch (selectedOption) {
                            case NO:
                                break;
                            case YES:
                                System.exit(0);
                            default:
                                break;
                        }
                    }
                } else {
                    promptToSave();
                }
            } catch (IOException ex) {
                Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.exit(0);
        }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() {
        PropertyManager manager = applicationTemplate.manager;
        XYChart chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();
        WritableImage screenshotImage = chart.snapshot(new SnapshotParameters(), null);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.FILE_CHOOSER_TITLE.name()));
        FileChooser.ExtensionFilter filter
                = new FileChooser.ExtensionFilter("PNG files (.png)", ".png");
        fileChooser.getExtensionFilters().add(filter);

        File selectedFile = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        if (selectedFile != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(screenshotImage, null), "png", selectedFile);
            } catch (IOException ex) {
                Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This helper method verifies that the user really wants to save their
     * unsaved work, which they might not want to do. The user will be presented
     * with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and
     * continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the
     * action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to
     * continue with the action, but also does not want to save the work at this
     * point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and
     * <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {

        ConfirmationDialog dialogue = ConfirmationDialog.getDialog();
        dialogue.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.WARNING.name()),
                applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));
        Option selectedOption = dialogue.getSelectedOption();

        if (selectedOption != null) {
            switch (selectedOption) {
                case NO:
                    System.exit(0);
                case YES:
                    handleSaveRequest();
                    System.exit(0);
                default:
                    break;
            }
        }
        return false;
    }

}
