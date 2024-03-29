package project3;

import enums.BoardElement;
import enums.EAdultSelection;
import enums.EParentSelection;
import enums.EProblemSelection;
import general.AbstractHypothesis;
import general.Values;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by markus on 21.02.2016.
 */
@SuppressWarnings("unchecked")
public class P3GUIController {

    NumberAxis xAxis = new NumberAxis(0, Values.POPULATION_SIZE + Values.NUMBER_OF_ELITES - 1, 2);
    NumberAxis yAxis = new NumberAxis(0, 1, 0.1);

    LineChart<Number, Number> populationFitnessLineChart = new LineChart<>(xAxis, yAxis);
    final NumberAxis xAxis2 = new NumberAxis();
    final NumberAxis yAxis2 = new NumberAxis(0, 1, 0.1);

    final LineChart<Number, Number> maxFitnessLineChart = new LineChart<>(xAxis2, yAxis2);
    final NumberAxis xAxis3 = new NumberAxis();
    final NumberAxis yAxis3 = new NumberAxis(0, 1, 0.1);

    final LineChart<Number, Number> avarageFitnessLineChart = new LineChart<>(xAxis3, yAxis3);
    final NumberAxis xAxis4 = new NumberAxis();
    final NumberAxis yAxis4 = new NumberAxis();
    final LineChart<Number, Number> stdFitnessLineChart = new LineChart<>(xAxis4, yAxis4);

    XYChart.Series<Number, Number> populationSeries;
    XYChart.Series<Number, Number> maxFitnessSeries;
    XYChart.Series<Number, Number> avgSeries;
    XYChart.Series<Number, Number> stdSeries;

    private TextArea consoleTextArea;

    // Fields needed to calculate FPS
    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;
    private P3Main p3MainClass;

    private TextField adultSizeTextField;
    private TextField tournamentGroupSize;
    private TextField tournamentEpsilon;
    private Label tournamentGroupSizeLabel;
    private Label tournamentEpsilonLabel;

    private BorderPane mainPane;
    private GridPane boardGridPane;
    private VBox boardVBox;
    private Label iterationsLabel;
    private Label poisonEatenLabel;
    private Label foodEatenLabel;

    public Pane generateGUI(P3Main p3Main) {
        p3MainClass = p3Main;
        mainPane = new BorderPane();

        VBox boardVBox = getCenterGUI(0);

        mainPane.setCenter(boardVBox);

        GridPane gridPane = getCharts();
        mainPane.setRight(gridPane);

        VBox controlPanelVBox = getControlPanelVBox();
        mainPane.setLeft(controlPanelVBox);

        consoleTextArea = new TextArea();
        consoleTextArea.setEditable(false);

        mainPane.setBottom(consoleTextArea);

        updateProblemSpesificGUIElementVisibilities();

        return mainPane;
    }



    private VBox getControlPanelVBox() {
        VBox vBox = new VBox();
        vBox.setPrefWidth(200);


        generateProblemSpesificGUI(vBox);

        Label EAVariablesLAbel = addLabel(vBox, "General Variables");
        EAVariablesLAbel.setFont(new Font(15));
        addLabel(vBox, "Population Size");
        TextField populationSizeTextField = new TextField();
        populationSizeTextField.setText(String.valueOf(Values.POPULATION_SIZE));
        populationSizeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            int newProblemSize;
            try{
                newProblemSize = Integer.parseInt(newValue);
            }catch (Exception E){
                newProblemSize = Integer.parseInt(oldValue);
            }
            Values.POPULATION_SIZE = newProblemSize;
            Values.MAX_ADULT_SIZE = newProblemSize / 2;
            Values.NUMBER_OF_ELITES = (int) (Values.POPULATION_SIZE * 0.05);
            Values.MAX_PARENT_SIZE = Values.POPULATION_SIZE / 2;
            adultSizeTextField.setText(String.valueOf(newProblemSize/2));
        });
        vBox.getChildren().add(populationSizeTextField);

        addLabel(vBox, "Adult Size");
        adultSizeTextField = new TextField();
        adultSizeTextField.setText(String.valueOf(Values.MAX_ADULT_SIZE));
        adultSizeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            int newProblemSize;
            try{
                newProblemSize = Integer.parseInt(newValue);
            }catch (Exception E){
                newProblemSize = Integer.parseInt(oldValue);
            }
            Values.MAX_ADULT_SIZE = newProblemSize;
        });
        vBox.getChildren().add(adultSizeTextField);

        addLabel(vBox, "Crossover propability");
        TextField crossoverTextField = new TextField();
        crossoverTextField.setText(String.valueOf(Values.CROSSOVER_PROBABILITY));
        crossoverTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            double newProblemSize;
            try{
                newProblemSize = Double.parseDouble(newValue);
            }catch (Exception E){
                newProblemSize = Double.parseDouble(oldValue);
            }
            Values.CROSSOVER_PROBABILITY = newProblemSize;
        });
        vBox.getChildren().add(crossoverTextField);

        addLabel(vBox, "Mutation propability");
        TextField mutationTextField = new TextField();
        mutationTextField.setText(String.valueOf(Values.MUTATION_PROBABILITY));
        mutationTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            double newProblemSize;
            try{
                newProblemSize = Double.parseDouble(newValue);
            }catch (Exception E){
                newProblemSize = Double.parseDouble(oldValue);
            }
            Values.MUTATION_PROBABILITY = newProblemSize;
        });
        vBox.getChildren().add(mutationTextField);





        addLabel(vBox, "Adult selection");
        ComboBox<EAdultSelection> adultSelectionComboBox = new ComboBox<>();
        adultSelectionComboBox.setValue(Values.ADULT_SELECTION);
        adultSelectionComboBox.getItems().setAll(EAdultSelection.values());
        adultSelectionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            Values.ADULT_SELECTION = newValue;
        });
        vBox.getChildren().add(adultSelectionComboBox);

        addLabel(vBox, "Parent selection");
        ComboBox<EParentSelection> parentSelectionComboBox = new ComboBox<>();
        parentSelectionComboBox.setValue(Values.PARENT_SELECTION);
        parentSelectionComboBox.getItems().setAll(EParentSelection.values());
        parentSelectionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            Values.PARENT_SELECTION = newValue;
            updateProblemSpesificGUIElementVisibilities();
        });
        vBox.getChildren().add(parentSelectionComboBox);

        tournamentGroupSizeLabel = addLabel(vBox, "Tournament group size");
        tournamentGroupSize = new TextField();
        tournamentGroupSize.setText(String.valueOf(Values.TOURNAMENT_SELECTION_GROUP_SIZE));
        tournamentGroupSize.textProperty().addListener((observable, oldValue, newValue) -> {
            int newProblemSize;
            try{
                newProblemSize = Integer.parseInt(newValue);
            }catch (Exception E){
                newProblemSize = Integer.parseInt(oldValue);
            }
            Values.TOURNAMENT_SELECTION_GROUP_SIZE = newProblemSize;
        });
        vBox.getChildren().add(tournamentGroupSize);

        tournamentEpsilonLabel = addLabel(vBox, "Torunament epsilon");
        tournamentEpsilon = new TextField();
        tournamentEpsilon.setText(String.valueOf(Values.TOURNAMENT_SELECTION_EPSILON));
        tournamentEpsilon.textProperty().addListener((observable, oldValue, newValue) -> {
            double newProblemSize;
            try{
                newProblemSize = Double.parseDouble(newValue);
            }catch (Exception E){
                newProblemSize = Double.parseDouble(oldValue);
            }
            Values.TOURNAMENT_SELECTION_EPSILON = newProblemSize;
        });
        vBox.getChildren().add(tournamentEpsilon);


        addLabel(vBox, "Print status every N generation:");
        TextField moduloChanger = new TextField();
        moduloChanger.setText(String.valueOf(Values.GENERATION_PRINT_THROTTLE));
        moduloChanger.textProperty().addListener((observable, oldValue, newValue) -> {
            int newProblemSize;
            try{
                newProblemSize = Integer.parseInt(newValue);
            }catch (Exception E){
                newProblemSize = Integer.parseInt(oldValue);
            }
            Values.GENERATION_PRINT_THROTTLE = newProblemSize;
        });
        vBox.getChildren().add(moduloChanger);

        CheckBox updateCharts = new CheckBox("Update charts");
        updateCharts.setOnAction(event -> {
            if (updateCharts.isSelected()){
                Values.UPDATE_CHARTS = true;
            }else{
                Values.UPDATE_CHARTS = false;
            }

        });
        updateCharts.setSelected(Values.UPDATE_CHARTS);
        vBox.getChildren().add(updateCharts);

        return vBox;
    }

    private void generateProblemSpesificGUI(VBox vBox) {
        HBox buttonHBox = new HBox();
        Button restartButton = new Button("New run");
        restartButton.setOnAction(event -> p3MainClass.restartAlgorithm());
        buttonHBox.getChildren().add(restartButton);

        Button pauseButton = getPauseSimulationButton();
        buttonHBox.getChildren().add(pauseButton);





        vBox.getChildren().add(buttonHBox);

        Label problemLabel = addLabel(vBox, "Problem Variables");
        problemLabel.setFont(new Font(15));

        addLabel(vBox, "Select problem");
        ComboBox<EProblemSelection> problemSelectionComboBox = new ComboBox<>();
        problemSelectionComboBox.setValue(Values.SELECTED_PROBLEM);
        problemSelectionComboBox.getItems().setAll(EProblemSelection.values());
        problemSelectionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            Values.SELECTED_PROBLEM = newValue;
            updateProblemSpesificGUIElementVisibilities();
        });
        vBox.getChildren().add(problemSelectionComboBox);

        addLabel(vBox, "Problem Size");
        TextField problemSizeTextField = new TextField();
        problemSizeTextField.setText(String.valueOf(Values.NUMBER_OF_BITS_IN_PROBLEM));
        problemSizeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            int newProblemSize;
            try{
                newProblemSize = Integer.parseInt(newValue);
            }catch (Exception E){
                newProblemSize = Integer.parseInt(oldValue);
            }
            Values.NUMBER_OF_BITS_IN_PROBLEM = newProblemSize;
        });
        vBox.getChildren().add(problemSizeTextField);


    }

    private void updateProblemSpesificGUIElementVisibilities() {

    }

    private Label addLabel(VBox vBox, String s) {
        Label label = new Label(s);
        vBox.getChildren().add(label);
        return label;
    }

    private GridPane getCharts() {
        GridPane gridPane = new GridPane();

        populationFitnessLineChart.setTitle("Population fitness");
        populationFitnessLineChart.setLegendVisible(false);
        populationFitnessLineChart.setPrefSize(500, 300);
        populationFitnessLineChart.setCreateSymbols(false);
        populationSeries = new XYChart.Series<>();
        populationFitnessLineChart.setAnimated(false);

        gridPane.add(populationFitnessLineChart, 0, 0);

        maxFitnessLineChart.setTitle("Best fitness");
        maxFitnessLineChart.setPrefSize(500, 300);
        maxFitnessLineChart.setAnimated(false);
        maxFitnessLineChart.setLegendVisible(false);
        maxFitnessLineChart.setCreateSymbols(false);

        maxFitnessSeries = new XYChart.Series<>();
        maxFitnessLineChart.getData().add(maxFitnessSeries);

        gridPane.add(maxFitnessLineChart, 1, 0);

        avarageFitnessLineChart.setTitle("Avarage fitness");
        avarageFitnessLineChart.setPrefSize(500, 300);
        avarageFitnessLineChart.setAnimated(false);
        avarageFitnessLineChart.setLegendVisible(false);
        avarageFitnessLineChart.setCreateSymbols(false);

        avgSeries = new XYChart.Series<>();
        avarageFitnessLineChart.getData().add(avgSeries);

        gridPane.add(avarageFitnessLineChart, 0, 1);


        stdFitnessLineChart.setTitle("Standard deviation fitness");
        stdFitnessLineChart.setPrefSize(500, 300);
        stdFitnessLineChart.setAnimated(false);
        stdFitnessLineChart.setLegendVisible(false);
        stdFitnessLineChart.setCreateSymbols(false);
        stdSeries = new XYChart.Series<>();
        stdFitnessLineChart.getData().add(stdSeries);

        gridPane.add(stdFitnessLineChart, 1, 1);



        return gridPane;
    }

    void updateLineCharts(List<AbstractHypothesis> population, double maxFitness, double avgFitness, double stdFitness, int generation, String phenoTypeString) {
        //noinspection unchecked
        if (Values.UPDATE_CHARTS){
            populationFitnessLineChart.getData().retainAll();

            populationSeries = new XYChart.Series<>();
            for (int i = 0; i < population.size(); i++) {
                populationSeries.getData().add(new XYChart.Data<>(i, (population.get(i)).getFitness()));
            }
            populationFitnessLineChart.getData().add(populationSeries);

            maxFitnessSeries.getData().add(new XYChart.Data<>(generation, maxFitness));
            avgSeries.getData().add(new XYChart.Data<>(generation, avgFitness));
            stdSeries.getData().add(new XYChart.Data<>(generation, stdFitness));
        }

        consoleTextArea.appendText("Gen.:  " + String.format("%03d", generation) +
                " \tBest fitness:  " + String.format("%4.3f", maxFitness) +
                " \tAvg fitness:   " + String.format("%4.3f", avgFitness) +
                " \tStandard deviation:  " + String.format("%.3f", stdFitness) +
                " \tPhenotype:  " + phenoTypeString +
                "\n");/**/

    }

    void appendTextToConsole(String s) {
        consoleTextArea.appendText(s);
    }

    void updateFPS(long now, Stage primaryStage) {
        String fpsString = getFPSString(now);
        primaryStage.setTitle(fpsString);
    }

    private String getFPSString(long now) {
        long oldFrameTime = frameTimes[frameTimeIndex];
        frameTimes[frameTimeIndex] = now;
        frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
        if (frameTimeIndex == 0) {
            arrayFilled = true;
        }
        if (arrayFilled) {
            long elapsedNanos = now - oldFrameTime;
            long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
            double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;
            String fpsString = String.format("Generations created per second: %.0f", frameRate);
            return fpsString;
        }
        return "";
    }

    private Button getPauseSimulationButton() {
        Button restartButton = new Button("Pause simulation");
        restartButton.setOnAction(event -> {
            if (p3MainClass.simulationPaused) {
                restartButton.setText("Pause simulation");
                p3MainClass.simulationPaused = false;
            } else {
                p3MainClass.simulationPaused = true;
                restartButton.setText("Start simulation");
            }
        });
        return restartButton;
    }



    public void clearGUI() {

        xAxis.setUpperBound(Values.POPULATION_SIZE + Values.NUMBER_OF_ELITES - 1);
        stdFitnessLineChart.getData().retainAll();
        stdSeries = new XYChart.Series<>();
        stdFitnessLineChart.getData().add(stdSeries);

        maxFitnessLineChart.getData().retainAll();
        maxFitnessSeries = new XYChart.Series<>();
        maxFitnessLineChart.getData().add(maxFitnessSeries);

        avarageFitnessLineChart.getData().retainAll();
        avgSeries = new XYChart.Series<>();
        avarageFitnessLineChart.getData().add(avgSeries);

        consoleTextArea.appendText("\n");
        consoleTextArea.appendText("\n");
        consoleTextArea.appendText("### RESTART ###");
        consoleTextArea.appendText("\n");
        consoleTextArea.appendText("\n");

    }

    public void drawMovement(int numberOfMoves) {


        ArrayList<BoardElement> sensorValues = Values.BOARD.getSensors();
        int highestIndex = Values.ANN.getMove(sensorValues);

        if (highestIndex == 0){
            Values.BOARD.moveForeward();
        }else if (highestIndex == 1){
            Values.BOARD.moveLeft();

        }
        else if (highestIndex == 2){
            Values.BOARD.moveRight();
        }

        boardVBox.getChildren().remove(boardGridPane);
        boardGridPane = Values.BOARD.generateBoardGridPane();
        boardVBox.getChildren().add(boardGridPane);

        updateBoardValues(numberOfMoves);

    }

    private void updateBoardValues(int numberOfMoves) {
        iterationsLabel.setText(String.valueOf(numberOfMoves + 1));
        foodEatenLabel.setText(String.valueOf(Values.BOARD.getFoodEaten()));
        poisonEatenLabel.setText( String.valueOf(Values.BOARD.getPoisonEaten()));


    }

    private VBox getCenterGUI(int numberOfMoves) {
        boardVBox = new VBox();
        boardGridPane = Values.BOARD.generateBoardGridPane();
        boardVBox.getChildren().add(boardGridPane);

        HBox iterationsHBox = new HBox();
        iterationsLabel = new Label();
        iterationsLabel.setText(String.valueOf(numberOfMoves + 1));
        iterationsHBox.getChildren().add(new Label("Iterations:\t"));
        iterationsHBox.getChildren().add(iterationsLabel);
        boardVBox.getChildren().add(iterationsHBox);

        HBox foodEatenHBox = new HBox();
        foodEatenLabel = new Label();
        foodEatenLabel.setText(String.valueOf(Values.BOARD.getFoodEaten()));
        foodEatenHBox.getChildren().add(new Label("Food eaten:\t"));
        foodEatenHBox.getChildren().add(foodEatenLabel);
        boardVBox.getChildren().add(foodEatenHBox);

        HBox poisonEatenHBox = new HBox();
        poisonEatenLabel = new Label();
        poisonEatenLabel.setText( String.valueOf(Values.BOARD.getPoisonEaten()));
        poisonEatenHBox.getChildren().add(new Label("Poison eaten:\t"));
        poisonEatenHBox.getChildren().add(poisonEatenLabel);
        boardVBox.getChildren().add(poisonEatenHBox);

        addLabel(boardVBox, "Sleep duration");
        TextField sleepDurationTextField = new TextField();
        sleepDurationTextField.setText(String.valueOf(Values.FLATLAND_SLEEP_DURATION));
        sleepDurationTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try{
                Values.FLATLAND_SLEEP_DURATION = Integer.parseInt(newValue);
            }catch (Exception E){
                Values.FLATLAND_SLEEP_DURATION = Integer.parseInt(oldValue);
            }
        });
        boardVBox.getChildren().add(sleepDurationTextField);


        CheckBox dynamicCheckBox = new CheckBox("Dynamic board");
        dynamicCheckBox.setOnAction(event -> {
            Values.FLATLAND_DYNAMIC = dynamicCheckBox.isSelected();

        });
        dynamicCheckBox.setSelected(Values.FLATLAND_DYNAMIC);
        boardVBox.getChildren().add(dynamicCheckBox);

        return boardVBox;
    }
}
