package ui;

import actions.AppActions;
import algorithms.Classifier;
import algorithms.Clusterer;
import classification.RandomClassifier;
import clustering.KMeansClusterer;
import clustering.RandomClusterer;
import data.DataSet;
import dataprocessors.AppData;
import static java.io.File.separator;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import settings.AppPropertyTypes;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import static vilij.settings.PropertyTypes.EXIT_TOOLTIP;
import static vilij.settings.PropertyTypes.LOAD_TOOLTIP;
import static vilij.settings.PropertyTypes.NEW_TOOLTIP;
import static vilij.settings.PropertyTypes.PRINT_TOOLTIP;
import static vilij.settings.PropertyTypes.SAVE_TOOLTIP;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private LineChart<Number, Number> chart;          // the chart where data will be displayed
    private Button displayButton;  // workspace button to display data on the chart
    private TextArea textArea;       // text area for new data input
    private String screenshoticonPath;
    private Button screenshotButton;
    private static AtomicBoolean isAlogRunning = new AtomicBoolean(false);
    private ArrayList<Integer> clustData = new ArrayList<>();
    private ArrayList<Integer> classData = new ArrayList<>();
    private ArrayList<Integer> kMeansData = new ArrayList<>();
    private ArrayList<Integer> count = new ArrayList<>();
    private RadioButton randomCluss;
    private RadioButton algorithmAClass;
    private RadioButton kMeansCluss;
    private Button clussGearbutton = new Button();
    private Button kMeansGearButton = new Button();
    private Button classGearbutton = new Button();
    private VBox vBox1 = new VBox();
    private Label metaDataLabel;
    private static ArrayList<String> labelNames = new ArrayList<String>();
    private Button clustering;
    private Button classification;
    private HBox ButtonBox = new HBox();
    private CheckBox readOnly = new CheckBox("Edit");
    private Button run;
    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();
    private DataSet dataSet;

    public TextArea getTextArea() {
        return textArea;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public Button getScreenshotButton() {
        return screenshotButton;
    }

    public AtomicBoolean getIsAlogRunning() {
        return isAlogRunning;
    }

    public Button getRun() {
        return run;
    }

    /**
     * disables/enables the save button upon the passed argument.
     *
     * @param b boolean argument passed by the user.
     */
    public void saveButtonDisable(boolean b) {
        saveButton.setDisable(b);
    }

    private void changeCursor() {
        //    changes the mouse when hovered through thw data points on the graph
        for (int j = 0; j < chart.getData().size(); j++) {
            XYChart.Series series = chart.getData().get(j);
            ObservableList<XYChart.Data<Number, Number>> o = series.getData();

            for (int i = 0; i < o.size(); i++) {

                o.get(i).getNode().setOnMouseEntered(new EventHandler<javafx.scene.input.MouseEvent>() {
                    @Override
                    public void handle(javafx.scene.input.MouseEvent event) {
                        applicationTemplate.getUIComponent().getPrimaryWindow().getScene().setCursor(Cursor.HAND);
                    }
                });

                o.get(i).getNode().setOnMouseExited(new EventHandler<javafx.scene.input.MouseEvent>() {
                    @Override
                    public void handle(javafx.scene.input.MouseEvent event) {
                        applicationTemplate.getUIComponent().getPrimaryWindow().getScene().setCursor(Cursor.DEFAULT);
                    }
                });
            }
        }
    }

    public String metaData(String line) {

        String metaData = "";
        String[] lines;

        for (int i = 0; i < line.split("\n").length; i++) {                   // outer loop for looping through all the lines of the file

            lines = line.split("[\t\n]");                                       // splitting each loines of the file into each tabs for easy access of label names
            for (int j = 0; j < lines.length; j++) {                                     // inner loop for looping through all the tabs in each line

                if (j == (1 + 3 * i)) {                                       // referencing only the label names present in te whole file.
                    String check = lines[j];
                    if (!labelNames.contains(check)) {                         // checking if the arraylist contains the label name: if it does... 
                        labelNames.add(check);                                  // add to the array list
                    }
                }
            }
        }
        int i = 0;
        metaData += "\n\n" + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.NO_OF_INSTANCES.name())
                + line.split("\n").length + "\n" + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.NO_OF_LABELS.name())
                + labelNames.size() + "\n" + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.LABELNAMES.name()) + "\n";
        while (i < labelNames.size()) {
            metaData += "\n\t" + labelNames.get(i);
            i++;
        }
        return metaData;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;

        xAxis.forceZeroInRangeProperty().setValue(Boolean.FALSE);
        yAxis.forceZeroInRangeProperty().setValue(Boolean.FALSE);

        chart = new LineChart<Number, Number>(xAxis, yAxis);

        Button displayButton = new Button();
        TextArea textArea = new TextArea();
        metaDataLabel = new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.META_DATA.name()));
        clustering = new Button(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name()));
        classification = new Button(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()));
        run = new Button(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.RUN_ALGORITHM.name()));

    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator, manager.getPropertyValue(AppPropertyTypes.GUI_ICONS_RESOURCE_PATH.name()));
        screenshoticonPath = String.join(separator, iconsPath, manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));

        newButton = setToolbarButton(newiconPath, manager.getPropertyValue(NEW_TOOLTIP.name()), false);
        saveButton = setToolbarButton(saveiconPath, manager.getPropertyValue(SAVE_TOOLTIP.name()), true);
        loadButton = setToolbarButton(loadiconPath, manager.getPropertyValue(LOAD_TOOLTIP.name()), false);
        printButton = setToolbarButton(printiconPath, manager.getPropertyValue(PRINT_TOOLTIP.name()), true);
        exitButton = setToolbarButton(exiticonPath, manager.getPropertyValue(EXIT_TOOLTIP.name()), false);
        screenshotButton = super.setToolbarButton(screenshoticonPath, manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()), true);
        toolBar = new ToolBar(newButton, saveButton, loadButton, printButton, screenshotButton, exitButton);

    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        screenshotButton.setOnAction(e -> ((AppActions) (applicationTemplate.getActionComponent())).handleScreenshotRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    public Label getMetaDataLabel() {
        return metaDataLabel;
    }

    private void layout() {

        // the second vertical box on the right.
        readOnly.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EDIT_ENTERED_DATA.name())));

        readOnly.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_val, Boolean new_val) {

                if (readOnly.isSelected()) {
                    textArea.setDisable(false);
                } else {
                    textArea.setDisable(true);
                }

            }
        });

        HBox hBox = new HBox();                             // the whole outerhorizontal box .
        VBox vBox2 = new VBox();
        VBox clusteringBox = new VBox();
        VBox classificationBox = new VBox();
        randomCluss = new RadioButton("Random "
                + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name()));
        kMeansCluss = new RadioButton("K Means "
                + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name()));
        algorithmAClass = new RadioButton(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ALGORITHM.name()) + " A");
        Label TextAreaLabel = new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE.name()));
        Label chartLabel = new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_VISUALISATION.name()));
        hBox.setFillHeight(true);
        vBox1.setMinWidth(450);

        textArea = new TextArea();
        textArea.setPrefRowCount(11);
        chartLabel.setFont(Font.font(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CAMBRIA.name()), 30));
        chartLabel.setTextAlignment(TextAlignment.CENTER);
        TextAreaLabel.setFont(Font.font(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CAMBRIA.name()), 20));
        TextAreaLabel.setTextAlignment(TextAlignment.CENTER);

        clussGearbutton.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name())
                + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ALGORITHM.name())
                + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIGURATION.name())));

        kMeansGearButton.setTooltip(new Tooltip("K means clusterer"));

        classGearbutton.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name())
                + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ALGORITHM.name())
                + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIGURATION.name())));

        run.setDisable(true);
        clusteringBox.setSpacing(10);
        classificationBox.setSpacing(10);
        clussGearbutton.setDisable(true);
        classGearbutton.setDisable(true);
        kMeansGearButton.setDisable(true);
        ButtonBox.setSpacing(10);

        VBox clussGearBox = new VBox();
        HBox randomClusteringOption = new HBox();
        HBox kMeansClusteringOption = new HBox();
        randomClusteringOption.getChildren().addAll(randomCluss, clussGearbutton);
        kMeansClusteringOption.getChildren().addAll(kMeansCluss, kMeansGearButton);
        clussGearBox.getChildren().addAll(randomClusteringOption, kMeansClusteringOption);
        clusteringBox.getChildren().addAll(clustering, clussGearBox, run);
        clussGearBox.setSpacing(10);
        clussGearBox.setVisible(false);

        HBox classGearBox = new HBox();
        classGearBox.getChildren().addAll(algorithmAClass, classGearbutton);
        classificationBox.getChildren().addAll(classification, classGearBox);
        ButtonBox.getChildren().addAll(clusteringBox, classificationBox);
        classGearBox.setSpacing(10);
        classGearBox.setVisible(false);

        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator, manager.getPropertyValue(AppPropertyTypes.GUI_ICONS_RESOURCE_PATH.name()));
        String gearIconPath = String.join(separator, iconsPath, manager.getPropertyValue(AppPropertyTypes.GEAR_ICON.name()));

        ImageView gearIcon1 = new ImageView(new Image(gearIconPath));
        ImageView gearIcon2 = new ImageView(new Image(gearIconPath));
        ImageView gearIcon3 = new ImageView(new Image(gearIconPath));

        gearIcon1.setFitHeight(15);
        gearIcon1.setFitWidth(15);
        gearIcon2.setFitHeight(15);
        gearIcon2.setFitWidth(15);
        gearIcon3.setFitHeight(15);
        gearIcon3.setFitWidth(15);
        clussGearbutton.setGraphic(gearIcon1);
        classGearbutton.setGraphic(gearIcon2);
        kMeansGearButton.setGraphic(gearIcon3);

        try {
            vBox1.getChildren().addAll(TextAreaLabel, textArea, displayButton);
            vBox2.getChildren().add(chart);
        } catch (NullPointerException e) {

            Insets inset = new Insets(2, 2, 4, 2);
            displayButton = new Button(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DISPLAY_TOOLTIP.name()));
            displayButton.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DISPLAY_TOOLTIP.name())));
            textArea.setMaxWidth(275);
            textArea.setPadding(inset);

            HBox box = new HBox();
            box.setSpacing(70);
            box.getChildren().addAll(displayButton, readOnly);

            Insets vBoxInset = new Insets(10, 10, 10, 10);
            vBox1.setPadding(vBoxInset);
            vBox1.setSpacing(10);
            vBox1.getChildren().addAll(TextAreaLabel, textArea, box, metaDataLabel);
            vBox2.getChildren().addAll(chartLabel, chart);
            hBox.getChildren().addAll(vBox1, vBox2);

        }

        for (int i = 0; i < 5; i++) {
            clustData.add(i, 0);
            classData.add(i, 0);
            kMeansData.add(i, 0);
            count.add(i, 0);
        }
        classification.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name())));
        classification.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                classGearBox.setVisible(true);
                clussGearBox.setVisible(false);

                algorithmAClass.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        classGearbutton.setDisable(!algorithmAClass.isSelected());
                        if (count.get(0) > 0) {
                            run.setDisable(!algorithmAClass.isSelected());
                        }
                    }
                });

                xAxis.forceZeroInRangeProperty().setValue(Boolean.TRUE);
                yAxis.forceZeroInRangeProperty().setValue(Boolean.TRUE);

                xAxis.forceZeroInRangeProperty().setValue(Boolean.FALSE);
                yAxis.forceZeroInRangeProperty().setValue(Boolean.FALSE);

            }
        });

        clustering.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name())));
        clustering.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clussGearBox.setVisible(true);
                classGearBox.setVisible(false);

                randomCluss.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        clussGearbutton.setDisable(!randomCluss.isSelected());
                        if (count.get(1) > 0) {
                            run.setDisable(!randomCluss.isSelected());
                        }

                    }
                });

                kMeansCluss.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        kMeansGearButton.setDisable(!kMeansCluss.isSelected());
                        if (count.get(2) > 0) {
                            run.setDisable(!kMeansCluss.isSelected());
                        }

                    }
                });

                xAxis.forceZeroInRangeProperty().setValue(Boolean.TRUE);
                yAxis.forceZeroInRangeProperty().setValue(Boolean.TRUE);

                xAxis.forceZeroInRangeProperty().setValue(Boolean.FALSE);
                yAxis.forceZeroInRangeProperty().setValue(Boolean.FALSE);

            }
        });

        if (textArea.getText().length() == 0) {
            vBox1.setVisible(false);
        }

        /* This snippet of code observes changes in the textArea (to check for the presence of any data) and then enables/disables 
                the save button accoringly.
         */
        textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {

                if (newValue.length() > 0 && oldValue.compareTo(newValue) != 0) {
                    saveButton.setDisable(false);
                } else {
                    saveButton.setDisable(true);
                }

                if (oldValue.compareTo(newValue) != 0) {
                    if (newValue.compareTo("") != 0) {
                        saveButton.setDisable(false);
                    }

                    displayButton.setOnAction(new EventHandler<ActionEvent>() {

                        public void handle(ActionEvent e) {

                            AppData appData = (AppData) applicationTemplate.getDataComponent();
                            try {
                                chart.getData().clear();
                                appData.loadData(newValue);
                                appData.displayData();
                                readOnly.setSelected(false);
                                textArea.setDisable(true);

                                for (int i = 0; i < textArea.getText().split("\n").length; i++) {
                                    dataSet.addInstance(textArea.getText().split("\n")[i]);
                                }
                                
                                if (!vBox1.getChildren().contains(ButtonBox)) {
                                    vBox1.getChildren().addAll(ButtonBox);

                                } else if (!ButtonBox.isVisible() && !textArea.getText().isEmpty()) {
                                    ButtonBox.setVisible(true);

                                }

                                metaDataLabel.setText(metaData(textArea.getText()));

                                if (labelNames.size() == 2) {
                                    classification.setDisable(false);
                                } else {
                                    classification.setDisable(true);
                                }

                            } catch (Exception ex) {
                                Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            if (chart.getData().isEmpty()) {
                                screenshotButton.setDisable(true);
                            } else {
                                screenshotButton.setDisable(false);
                            }

                            changeCursor();

                        }
                    });

                }
                if (!vBox1.getChildren().contains(ButtonBox)) {
                    vBox1.getChildren().add(ButtonBox);
                }

            }

        });

        appPane.getStylesheets().add(getClass().getResource(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CHARTCSS.name())).toExternalForm());
        appPane.getChildren().add(hBox);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    private void setWorkspaceActions() {

        newButton.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent e) {

                vBox1.setVisible(true);
                applicationTemplate.getActionComponent().handleNewRequest();
                textArea.setDisable(false);
                ButtonBox.setVisible(false);
                metaDataLabel.setText("");
                labelNames.clear();
                changeCursor();

            }
        });

        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                labelNames.clear();
                classification.setDisable(false);
                vBox1.setVisible(true);
                ButtonBox.setVisible(true);
                applicationTemplate.getActionComponent().handleLoadRequest();

                if (labelNames.size() != 2) {
                    classification.setDisable(true);
                }

                if (!chart.getData().isEmpty()) {
                    screenshotButton.setDisable(false);
                } else {
                    screenshotButton.setDisable(true);
                }
                changeCursor();
            }
        });

        classGearbutton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                Stage algorithmConfigurationStage = new Stage();
                algorithmConfigurationStage.setAlwaysOnTop(true);
                algorithmConfigurationStage.setTitle(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name())
                        + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ALGORITHM.name())
                        + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIGURATION.name()));

                VBox classAlgoConfigBox = new VBox();
                classAlgoConfigBox.setSpacing(20);
                TextField maxIterations = new TextField();
                TextField updateInterval = new TextField();
                CheckBox continuousRun = new CheckBox();

                HBox hBox1 = new HBox();
                hBox1.alignmentProperty().set(Pos.CENTER);
                hBox1.setDisable(false);
                hBox1.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS.name())), maxIterations);
                hBox1.setSpacing(50);

                HBox hBox2 = new HBox();
                hBox2.alignmentProperty().set(Pos.CENTER);
                hBox2.setDisable(false);
                hBox2.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATE_INTERVAL.name())), updateInterval);
                hBox2.setSpacing(50);

                HBox hBox3 = new HBox();
                hBox3.alignmentProperty().set(Pos.CENTER);
                hBox3.setDisable(false);
                hBox3.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONTINUOUS_RUN.name())), continuousRun);
                hBox3.setSpacing(50);

                classAlgoConfigBox.setAlignment(Pos.CENTER);

                Button save = new Button(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVECONFIG.name()));
                save.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVECONFIG.name())));

                /*
                loading the previously present configuration data.
                 */
                maxIterations.setText("" + classData.get(0));
                updateInterval.setText("" + classData.get(1));

                if (classData.get(2) == 1) {
                    continuousRun.setSelected(true);
                }

                save.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        ErrorDialog warning = ErrorDialog.getDialog();
                        warning.setAlwaysOnTop(true);
                        boolean noError = true;
                        if (maxIterations.getText().compareTo("") == 0
                                || updateInterval.getText().compareTo("") == 0) {
                            warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EMPTY_FEILDS.name()),
                                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EMPTYTEXTFEILDS.name()));
                            noError = false;

                        } else if (maxIterations.getText().compareTo("0") == 0
                                || updateInterval.getText().compareTo("0") == 0) {
                            warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ZERO_VALUES.name()),
                                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ZEROVALUES.name()));
                            noError = false;

                        } else {

                            for (int i = 0; i < maxIterations.getText().length(); i++) {
                                if (!Character.isDigit(maxIterations.getText().charAt(i))) {
                                    warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()),
                                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAXITERATIONS.name()));
                                    noError = false;
                                }
                            }

                            for (int i = 0; i < updateInterval.getText().length(); i++) {
                                if (!Character.isDigit(updateInterval.getText().charAt(i))) {
                                    warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()),
                                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATEINTERVAL.name()));
                                    noError = false;
                                }
                            }
                        }

                        if (noError) {
                            try {
                                classData.add(0, Integer.parseInt(maxIterations.getText()));
                                classData.add(1, Integer.parseInt(updateInterval.getText()));
                                if (continuousRun.isSelected()) {
                                    classData.add(2, 1);
                                } else {
                                    classData.add(2, 0);
                                }
                                classData.add(3, Integer.parseInt(maxIterations.getText()));

                                hBox1.setDisable(noError);
                                hBox2.setDisable(noError);
                                hBox3.setDisable(noError);
                                run.setDisable(!noError);

                                algorithmConfigurationStage.close();

                            } catch (NumberFormatException e) {
                                Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, e);
                            }
                            count.set(0, count.get(0) + 1);
                        }
                    }
                });

                run.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        chart.getXAxis().setAutoRanging(true);
                        chart.getYAxis().setAutoRanging(true);
                        isAlogRunning.set(true);
                        run.setDisable(true);

                        Classifier randomClassifier = new RandomClassifier(
                                dataSet,
                                Integer.parseInt(maxIterations.getText()),
                                Integer.parseInt(updateInterval.getText()),
                                continuousRun.isSelected(),
                                applicationTemplate);

                        if (!continuousRun.isSelected()) {
                            if (RandomClassifier.j.get() == 1) {
                                ErrorDialog warning = ErrorDialog.getDialog();
                                warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.PRESS_RUN.name()),
                                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.PRESS_RUN_TEXT.name()));
                            }
                        }

                        Thread runThread = new Thread(randomClassifier);
                        try {
                            runThread.start();
                        } catch (Exception e) {
                            Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, e);
                        }

                        if (!continuousRun.isSelected()) {

                            ObjectProperty<Color> color = new SimpleObjectProperty<Color>(Color.GREY);

                            final StringBinding cssColorSpec = Bindings.createStringBinding(new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    return String.format(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.STRING_FORMAT.name()),
                                            (int) (256 * color.get().getRed()),
                                            (int) (256 * color.get().getGreen()),
                                            (int) (256 * color.get().getBlue()));
                                }
                            }, color);

                            // bind the button's style property
                            run.styleProperty().bind(cssColorSpec);
                            final Timeline timeline = new Timeline(
                                    new KeyFrame(Duration.ZERO, new KeyValue(color, Color.GREY)),
                                    new KeyFrame(Duration.seconds(1), new KeyValue(color, Color.YELLOWGREEN)));

                            timeline.play();

                            if (RandomClassifier.j.get() > Integer.parseInt(maxIterations.getText())) {
                                ErrorDialog warning = ErrorDialog.getDialog();
                                warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS_REACHED.name()),
                                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS_REACHED_TEXT.name()));
                                RandomClassifier.j.set(1);          // resetting the value of the counter for non continuous run.
                                isAlogRunning.set(false);
                                timeline.stop();
//                                run.styleProperty().bind(null);
                                // the algorithm has ended
                            }

                        }

                    }

                });

                classAlgoConfigBox.getChildren().addAll(hBox1, hBox2, hBox3, save);
                Scene configScene = new Scene(classAlgoConfigBox, 300, 300);
                algorithmConfigurationStage.setScene(configScene);
                algorithmConfigurationStage.show();

            }
        });

        /*
            this method is to allow the clustering algorithm menu to pop up in a new small window
         */
        clussGearbutton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                Stage algorithmConfigurationStage = new Stage();
                algorithmConfigurationStage.setAlwaysOnTop(true);
                algorithmConfigurationStage.setTitle(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name())
                        + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ALGORITHM.name())
                        + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIGURATION.name()));
                VBox clustAlgoConfigBox = new VBox();
                clustAlgoConfigBox.setSpacing(20);
                TextField maxIterations = new TextField();
                TextField updateInterval = new TextField();
                TextField clusters = new TextField();
                CheckBox continuousRun = new CheckBox();

                HBox hBox1 = new HBox();
                hBox1.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS.name())), maxIterations);
                hBox1.alignmentProperty().set(Pos.CENTER);
                hBox1.setSpacing(50);
                hBox1.setDisable(false);

                HBox hBox2 = new HBox();
                hBox2.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATE_INTERVAL.name())), updateInterval);
                hBox2.alignmentProperty().set(Pos.CENTER);
                hBox2.setSpacing(50);
                hBox2.setDisable(false);

                HBox hBox3 = new HBox();
                hBox3.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONTINUOUS_RUN.name())), continuousRun);
                hBox3.alignmentProperty().set(Pos.CENTER);
                hBox3.setSpacing(50);
                hBox3.setDisable(false);

                HBox hBox4 = new HBox();
                hBox4.alignmentProperty().set(Pos.CENTER);
                hBox4.getChildren().addAll(new Label("Clusters: "), clusters);
                hBox4.setDisable(false);

                hBox4.setSpacing(83);
                clustAlgoConfigBox.setAlignment(Pos.CENTER);
                Button save = new Button(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVECONFIG.name()));
                save.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVECONFIG.name())));

                /*
                loading the previously present configuration data.
                 */
                maxIterations.setText("" + clustData.get(0));
                updateInterval.setText("" + clustData.get(1));
                clusters.setText("" + clustData.get(3));

                if (clustData.get(2) == 1) {
                    continuousRun.setSelected(true);
                }

                /*
                when the save configuration button is clicked in the algorithm configuration dialog box
                 */
                save.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        /*
                        checking if the configuration settings are zero or empty.
                         */
                        ErrorDialog warning = ErrorDialog.getDialog();
                        warning.setAlwaysOnTop(true);
                        boolean noError = true;
                        if (maxIterations.getText().compareTo("") == 0
                                || updateInterval.getText().compareTo("") == 0
                                || clusters.getText().compareTo("") == 0) {

                            warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EMPTY_FEILDS.name()),
                                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EMPTYTEXTFEILDS.name()));
                            noError = false;

                        } else if (maxIterations.getText().compareTo("0") == 0
                                || updateInterval.getText().compareTo("0") == 0
                                || clusters.getText().compareTo("0") == 0) {
                            warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ZERO_VALUES.name()),
                                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ZEROVALUES.name()));
                            noError = false;
                        } else {

                            /*
                             checking whether there are any other characters other than digits.
                             */
                            for (int i = 0; i < maxIterations.getText().length(); i++) {
                                if (!Character.isDigit(maxIterations.getText().charAt(i))) {
                                    warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()),
                                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAXITERATIONS.name()));
                                    noError = false;
                                }
                            }

                            for (int i = 0; i < updateInterval.getText().length(); i++) {
                                if (!Character.isDigit(updateInterval.getText().charAt(i))) {
                                    warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()),
                                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATEINTERVAL.name()));
                                    noError = false;
                                }
                            }

                            for (int i = 0; i < clusters.getText().length(); i++) {
                                if (!Character.isDigit(clusters.getText().charAt(i))) {
                                    warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()),
                                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERS.name()));
                                    noError = false;
                                }
                            }
                        }

                        if (noError) {
                            try {
                                clustData.add(0, Integer.parseInt(maxIterations.getText()));
                                clustData.add(1, Integer.parseInt(updateInterval.getText()));
                                if (continuousRun.isSelected()) {
                                    clustData.add(2, 1);
                                } else {
                                    clustData.add(2, 0);
                                }
                                clustData.add(3, Integer.parseInt(clusters.getText()));

                                hBox1.setDisable(noError);
                                hBox2.setDisable(noError);
                                hBox3.setDisable(noError);
                                hBox4.setDisable(noError);
                                run.setDisable(!noError);

                                algorithmConfigurationStage.close();
                            } catch (NumberFormatException e) {
                                Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, e);
                            }
                            count.set(1, count.get(1) + 1);
                        }

                    }
                });

                run.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        chart.getXAxis().setAutoRanging(false);
                        chart.getYAxis().setAutoRanging(false);
                        isAlogRunning.set(true);
                        run.setDisable(true);

                        Clusterer randomClusterer = new RandomClusterer(
                                dataSet,
                                Integer.parseInt(maxIterations.getText()),
                                Integer.parseInt(updateInterval.getText()),
                                applicationTemplate,
                                continuousRun.isSelected(),
                                Integer.parseInt(clusters.getText()));

                        if (!continuousRun.isSelected()) {
                            if (RandomClusterer.j.get() == 1) {
                                ErrorDialog warning = ErrorDialog.getDialog();
                                warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.PRESS_RUN.name()),
                                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.PRESS_RUN_TEXT.name()));
                            }
                        }
                        Thread runThread = new Thread(randomClusterer);
                        try {
                            runThread.start();
                        } catch (Exception e) {
                            Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, e);
                        }

                        if (!continuousRun.isSelected()) {

                            ObjectProperty<Color> color = new SimpleObjectProperty<Color>(Color.GREY);

                            final StringBinding cssColorSpec = Bindings.createStringBinding(new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    return String.format(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.STRING_FORMAT.name()),
                                            (int) (256 * color.get().getRed()),
                                            (int) (256 * color.get().getGreen()),
                                            (int) (256 * color.get().getBlue()));
                                }
                            }, color);

                            // bind the button's style property
                            run.styleProperty().bind(cssColorSpec);
                            final Timeline timeline = new Timeline(
                                    new KeyFrame(Duration.ZERO, new KeyValue(color, Color.GREY)),
                                    new KeyFrame(Duration.seconds(1), new KeyValue(color, Color.YELLOWGREEN)));

                            timeline.play();

                            if (RandomClusterer.j.get() > Integer.parseInt(maxIterations.getText())) {
                                ErrorDialog warning = ErrorDialog.getDialog();
                                warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS_REACHED.name()),
                                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS_REACHED_TEXT.name()));
                                RandomClusterer.j.set(1);          // resetting the value of the counter for non continuous run.
                                isAlogRunning.set(false);           // the algorithm has ended
                            }
                        }
                    }
                });
                clustAlgoConfigBox.getChildren().addAll(hBox1, hBox2, hBox4, hBox3, save);
                Scene configScene = new Scene(clustAlgoConfigBox, 300, 300);
                algorithmConfigurationStage.setScene(configScene);
                algorithmConfigurationStage.show();
            }
        });

        /*
            this method is to allow the clustering algorithm menu to pop up in a new small window
         */
        kMeansGearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                Stage algorithmConfigurationStage = new Stage();
                algorithmConfigurationStage.setAlwaysOnTop(true);
                algorithmConfigurationStage.setTitle("KMeans"
                        + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ALGORITHM.name())
                        + " " + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIGURATION.name()));
                VBox kMeansAlgoConfigBox = new VBox();
                kMeansAlgoConfigBox.setSpacing(20);
                TextField maxIterations = new TextField();
                TextField updateInterval = new TextField();
                TextField clusters = new TextField();
                CheckBox continuousRun = new CheckBox();

                HBox hBox1 = new HBox();
                hBox1.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS.name())), maxIterations);
                hBox1.alignmentProperty().set(Pos.CENTER);
                hBox1.setSpacing(50);
                hBox1.setDisable(false);

                HBox hBox2 = new HBox();
                hBox2.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATE_INTERVAL.name())), updateInterval);
                hBox2.alignmentProperty().set(Pos.CENTER);
                hBox2.setSpacing(50);
                hBox2.setDisable(false);

                HBox hBox3 = new HBox();
                hBox3.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONTINUOUS_RUN.name())), continuousRun);
                hBox3.alignmentProperty().set(Pos.CENTER);
                hBox3.setSpacing(50);
                hBox3.setDisable(false);

                HBox hBox4 = new HBox();
                hBox4.alignmentProperty().set(Pos.CENTER);
                hBox4.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERS_NAME.name())), clusters);
                hBox4.setDisable(false);

                hBox4.setSpacing(83);
                kMeansAlgoConfigBox.setAlignment(Pos.CENTER);
                Button save = new Button(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVECONFIG.name()));
                save.setTooltip(new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVECONFIG.name())));

                /*
                loading the previously present configuration data.
                 */
                maxIterations.setText("" + kMeansData.get(0));
                updateInterval.setText("" + kMeansData.get(1));
                clusters.setText("" + kMeansData.get(3));

                if (kMeansData.get(2) == 1) {
                    continuousRun.setSelected(true);
                }

                /*
                when the save configuration button is clicked in the algorithm configuration dialog box
                 */
                save.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        /*
                        checking if the configuration settings are zero or empty.
                         */
                        ErrorDialog warning = ErrorDialog.getDialog();
                        warning.setAlwaysOnTop(true);
                        boolean noError = true;
                        if (maxIterations.getText().compareTo("") == 0
                                || updateInterval.getText().compareTo("") == 0
                                || clusters.getText().compareTo("") == 0) {

                            warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EMPTY_FEILDS.name()),
                                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EMPTYTEXTFEILDS.name()));
                            noError = false;

                        } else if (maxIterations.getText().compareTo("0") == 0
                                || updateInterval.getText().compareTo("0") == 0
                                || clusters.getText().compareTo("0") == 0) {
                            warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ZERO_VALUES.name()),
                                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ZEROVALUES.name()));
                            noError = false;
                        } else {

                            /*
                             checking whether there are any other characters other than digits.
                             */
                            for (int i = 0; i < maxIterations.getText().length(); i++) {
                                if (!Character.isDigit(maxIterations.getText().charAt(i))) {
                                    warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()),
                                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAXITERATIONS.name()));
                                    noError = false;
                                }
                            }

                            for (int i = 0; i < updateInterval.getText().length(); i++) {
                                if (!Character.isDigit(updateInterval.getText().charAt(i))) {
                                    warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()),
                                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATEINTERVAL.name()));
                                    noError = false;
                                }
                            }

                            for (int i = 0; i < clusters.getText().length(); i++) {
                                if (!Character.isDigit(clusters.getText().charAt(i))) {
                                    warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()),
                                            applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTERS.name()));
                                    noError = false;
                                }
                            }
                        }

                        if (noError) {
                            try {
                                kMeansData.add(0, Integer.parseInt(maxIterations.getText()));
                                kMeansData.add(1, Integer.parseInt(updateInterval.getText()));
                                if (continuousRun.isSelected()) {
                                    kMeansData.add(2, 1);
                                } else {
                                    kMeansData.add(2, 0);
                                }
                                kMeansData.add(3, Integer.parseInt(clusters.getText()));

                                hBox1.setDisable(noError);
                                hBox2.setDisable(noError);
                                hBox3.setDisable(noError);
                                hBox4.setDisable(noError);
                                run.setDisable(!noError);

                                algorithmConfigurationStage.close();
                            } catch (NumberFormatException e) {
                                Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, e);
                            }
                            count.set(2, count.get(2) + 1);
                        }
                    }
                });

                run.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        chart.getXAxis().setAutoRanging(false);
                        chart.getYAxis().setAutoRanging(false);
                        isAlogRunning.set(true);
                        run.setDisable(true);

                        Clusterer kMeansClusterer = new KMeansClusterer(
                                dataSet,
                                applicationTemplate,
                                Integer.parseInt(maxIterations.getText()),
                                Integer.parseInt(updateInterval.getText()),
                                continuousRun.isSelected(),
                                Integer.parseInt(clusters.getText()));

                        if (!continuousRun.isSelected()) {
                            if (KMeansClusterer.j.get() == 1) {
                                ErrorDialog warning = ErrorDialog.getDialog();
                                warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.PRESS_RUN.name()),
                                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.PRESS_RUN_TEXT.name()));
                            }
                        }

                        Thread runThread = new Thread(kMeansClusterer);
                        try {
                            runThread.start();
                        } catch (Exception e) {
                            Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, e);
                        }

                        if (!continuousRun.isSelected()) {

                            ObjectProperty<Color> color = new SimpleObjectProperty<Color>(Color.GREY);

                            final StringBinding cssColorSpec = Bindings.createStringBinding(new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    return String.format(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.STRING_FORMAT.name()),
                                            (int) (256 * color.get().getRed()),
                                            (int) (256 * color.get().getGreen()),
                                            (int) (256 * color.get().getBlue()));
                                }
                            }, color);

                            // bind the button's style property
                            run.styleProperty().bind(cssColorSpec);
                            final Timeline timeline = new Timeline(
                                    new KeyFrame(Duration.ZERO, new KeyValue(color, Color.GREY)),
                                    new KeyFrame(Duration.seconds(1), new KeyValue(color, Color.YELLOWGREEN)));

                            timeline.play();

                            if (KMeansClusterer.j.get() > Integer.parseInt(maxIterations.getText())) {
                                ErrorDialog warning = ErrorDialog.getDialog();
                                warning.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS_REACHED.name()),
                                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS_REACHED_TEXT.name()));
                                KMeansClusterer.j.set(1);          // resetting the value of the counter for non continuous run.
                                isAlogRunning.set(false);           // the algorithm has ended
                            }
                        }
                    }
                });
                kMeansAlgoConfigBox.getChildren().addAll(hBox1, hBox2, hBox4, hBox3, save);
                Scene configScene = new Scene(kMeansAlgoConfigBox, 300, 300);
                algorithmConfigurationStage.setScene(configScene);
                algorithmConfigurationStage.show();
            }
        });
    }
}
